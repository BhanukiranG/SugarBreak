package com.main.sugarbreak.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.main.sugarbreak.BuildConfig
import com.main.sugarbreak.domain.model.AuthenticatedUser
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

internal class ApiError(val status: Int) : Exception("SugarBreak server returned HTTP $status.")

@Singleton
class RestAuthenticationRepository @Inject constructor(@ApplicationContext context: Context) : AuthenticationRepository {
    private val secrets = context.getSharedPreferences("rest-session", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(
            ChuckerInterceptor.Builder(context)
                .redactHeaders("Authorization", "Cookie", "Set-Cookie")
                .maxContentLength(0L)
                .build()
        )
        .build()
    private val lock = Mutex()
    private var accessToken: String? = null
    override val currentUser = MutableStateFlow(restoreAccount())

    override fun currentUserSnapshot() = currentUser.value

    override suspend fun registerWithEmail(email: String, password: String) {
        call("POST", "/auth/register", credentials(email, password))
    }

    override suspend fun signInWithEmail(email: String, password: String) = lock.withLock {
        val reply = JSONObject(call("POST", "/auth/login", credentials(email, password)).decodeToString())
        persist(reply, email.trim())
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Unit = error("Use your SugarBreak email and password.")
    override suspend fun sendPasswordReset(email: String): Unit = error("Contact the SugarBreak administrator to recover your account.")
    override suspend fun resendVerificationEmail(): Unit = error("Email verification is unavailable.")
    override suspend fun refreshEmailVerification() = currentUser.value != null

    override suspend fun signOut() = lock.withLock {
        val refresh = readSession()?.optString("refreshToken")
        if (!refresh.isNullOrEmpty()) {
            // Keep the session available for retry if server-side revocation cannot be confirmed.
            call("POST", "/auth/logout", JSONObject().put("refreshToken", refresh).toString().encodeToByteArray())
        }
        clearSession()
    }

    internal suspend fun request(uid: String, method: String, path: String, body: ByteArray? = null, contentType: String = "application/json"): ByteArray {
        val token = lock.withLock { tokenFor(uid) }
        return try {
            call(method, path, body, token, contentType)
        } catch (error: ApiError) {
            if (error.status != 401) throw error
            val next = lock.withLock {
                check(currentUser.value?.uid == uid) { "The signed-in account changed." }
                if (accessToken == token) accessToken = null
                tokenFor(uid)
            }
            call(method, path, body, next, contentType)
        }
    }

    private suspend fun tokenFor(uid: String): String {
        check(currentUser.value?.uid == uid) { "The signed-in account changed." }
        accessToken?.let { return it }
        val saved = checkNotNull(readSession())
        val reply = try {
            JSONObject(call("POST", "/auth/refresh",
                JSONObject().put("refreshToken", saved.getString("refreshToken")).toString().encodeToByteArray()).decodeToString())
        } catch (error: ApiError) {
            if (error.status == 401) clearSession()
            throw error
        }
        check(reply.getString("userId") == uid)
        persist(reply, saved.getString("email"))
        return checkNotNull(accessToken)
    }

    private fun credentials(email: String, password: String) =
        JSONObject().put("email", email.trim()).put("password", password).toString().encodeToByteArray()

    private suspend fun call(method: String, path: String, body: ByteArray?, token: String? = null,
                             contentType: String = "application/json"): ByteArray = withContext(Dispatchers.IO) {
        val url = URL(BuildConfig.SUGARBREAK_API_URL.trimEnd('/') + "/api/v1" + path)
        require(url.protocol == "https") { "SugarBreak requires HTTPS." }
        val requestBody = body?.toRequestBody(contentType.toMediaType())
            ?: if (method == "POST" || method == "PUT") ByteArray(0).toRequestBody() else null
        val request = Request.Builder().url(url).method(method, requestBody).apply {
            if (token != null) header("Authorization", "Bearer $token")
        }.build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw ApiError(response.code)
            response.body?.bytes() ?: ByteArray(0)
        }
    }

    private fun persist(reply: JSONObject, email: String) {
        val record = JSONObject().put("uid", reply.getString("userId")).put("email", email)
            .put("refreshToken", reply.getString("refreshToken"))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encoded = Base64.encodeToString(cipher.iv + cipher.doFinal(record.toString().encodeToByteArray()), Base64.NO_WRAP)
        check(secrets.edit().putString("session", encoded).commit())
        accessToken = reply.getString("accessToken")
        currentUser.value = account(record)
    }

    private fun clearSession() {
        check(secrets.edit().remove("session").commit())
        accessToken = null
        currentUser.value = null
    }

    private fun restoreAccount() = readSession()?.let(::account)

    private fun account(record: JSONObject) = AuthenticatedUser(
        record.getString("uid"), record.getString("email"), null, true
    )

    private fun readSession() = runCatching {
        val saved = secrets.getString("session", null) ?: return null
        val bytes = Base64.decode(saved, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes, 0, 12))
        JSONObject(cipher.doFinal(bytes, 12, bytes.size - 12).decodeToString())
    }.getOrNull()

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return store.getKey("sugarbreak-api-session", null) as? SecretKey
            ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
                init(KeyGenParameterSpec.Builder("sugarbreak-api-session",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
                generateKey()
            }
    }
}
