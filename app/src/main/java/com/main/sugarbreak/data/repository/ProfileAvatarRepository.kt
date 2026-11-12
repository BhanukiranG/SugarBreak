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
    private val authentication: AuthenticationRepository
) {
    val avatar = observeAccountAvatars(
        authentication.currentUser.map { it?.takeIf { user -> user.emailVerified }?.uid },
        ::observe
    )

    fun isCurrentAccount(uid: String): Boolean =
        authentication.currentUserSnapshot()?.let { it.uid == uid && it.emailVerified } == true

    suspend fun save(uid: String, jpeg: ByteArray?) {
        check(isCurrentAccount(uid)) { "The signed-in account changed" }
        val reference = checkNotNull(services.firestore).collection("profileAvatars").document(uid)
        if (jpeg == null) {
            reference.delete().await()
        } else {
            require(jpeg.size in 1..MAX_AVATAR_BYTES)
            reference.set(mapOf("jpeg" to Blob.fromBytes(jpeg))).await()
        }
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
