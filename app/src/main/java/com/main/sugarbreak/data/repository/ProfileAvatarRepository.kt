package com.main.sugarbreak.data.repository

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.MetadataChanges
import com.main.sugarbreak.data.firebase.FirebaseServices
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.CancellationException
import com.main.sugarbreak.BuildConfig
import javax.inject.Provider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

internal const val MAX_AVATAR_BYTES = 64 * 1024

data class ProfileAvatar(
    val uid: String? = null,
    val jpeg: ByteArray? = null,
    val pending: Boolean = false,
    val readFailed: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
internal fun observeAccountAvatars(
    accounts: Flow<String?>,
    observe: (String) -> Flow<ProfileAvatar>
): Flow<ProfileAvatar> = accounts.distinctUntilChanged().flatMapLatest { uid ->
    if (uid == null) flowOf(ProfileAvatar()) else observe(uid).onStart { emit(ProfileAvatar(uid)) }
}

@Singleton
class ProfileAvatarRepository @Inject constructor(
    private val services: FirebaseServices,
    private val authentication: AuthenticationRepository,
    private val rest: Provider<RestAuthenticationRepository>? = null
) {
    private val restPhoto = MutableStateFlow(ProfileAvatar())
    val avatar = observeAccountAvatars(
        authentication.currentUser.map { it?.takeIf { user -> user.emailVerified }?.uid },
        { uid -> if (BuildConfig.SUGARBREAK_API_URL.isEmpty()) observe(uid) else observeRest(uid) }
    )

    fun isCurrentAccount(uid: String): Boolean =
        authentication.currentUserSnapshot()?.let { it.uid == uid && it.emailVerified } == true

    suspend fun save(uid: String, jpeg: ByteArray?) {
        check(isCurrentAccount(uid)) { "The signed-in account changed" }
        if (BuildConfig.SUGARBREAK_API_URL.isNotEmpty()) {
            if (jpeg != null) require(jpeg.size in 1..MAX_AVATAR_BYTES)
            checkNotNull(rest).get().request(uid, if (jpeg == null) "DELETE" else "PUT",
                "/me/avatar", jpeg, "image/jpeg")
            if (isCurrentAccount(uid)) restPhoto.value = ProfileAvatar(uid, jpeg)
            return
        }
        val reference = checkNotNull(services.firestore).collection("profileAvatars").document(uid)
        if (jpeg == null) {
            reference.delete().await()
        } else {
            require(jpeg.size in 1..MAX_AVATAR_BYTES)
            reference.set(mapOf("jpeg" to Blob.fromBytes(jpeg))).await()
        }
    }

    private fun observeRest(uid: String) = flow {
        val avatar = try {
            ProfileAvatar(uid, checkNotNull(rest).get().request(uid, "GET", "/me/avatar"))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: ApiError) {
            ProfileAvatar(uid, readFailed = error.status != 404)
        } catch (_: Exception) {
            ProfileAvatar(uid, readFailed = true)
        }
        if (isCurrentAccount(uid)) restPhoto.value = avatar
        emitAll(restPhoto.filter { it.uid == uid })
    }

    private fun observe(uid: String) = callbackFlow {
        val reference = services.firestore?.collection("profileAvatars")?.document(uid)
        if (reference == null) {
            trySend(ProfileAvatar(uid, readFailed = true))
            close()
            return@callbackFlow
        }
        val registration = reference.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (!isCurrentAccount(uid)) return@addSnapshotListener
            val jpeg = (snapshot?.get("jpeg") as? Blob)?.toBytes()?.takeIf { it.size in 1..MAX_AVATAR_BYTES }
            trySend(ProfileAvatar(uid, jpeg, snapshot?.metadata?.hasPendingWrites() == true, error != null))
        }
        awaitClose { registration.remove() }
    }
}
