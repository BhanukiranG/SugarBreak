package com.main.sugarbreak.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.main.sugarbreak.data.firebase.FirebaseServices
import com.main.sugarbreak.domain.model.AuthenticatedUser
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthenticationRepository @Inject constructor(
    private val firebaseServices: FirebaseServices
) : AuthenticationRepository {
    private val auth: FirebaseAuth? get() = firebaseServices.auth
    override val currentUser: Flow<AuthenticatedUser?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser.toDomain()) }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override fun currentUserSnapshot(): AuthenticatedUser? = auth?.currentUser.toDomain()

    override suspend fun registerWithEmail(email: String, password: String) {
        val user = requireAuth().createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Firebase did not return a user after registration")
        user.sendEmailVerification().await()
    }

    override suspend fun signInWithEmail(email: String, password: String) {
        requireAuth().signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun signInWithGoogleIdToken(idToken: String) {
        requireAuth().signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
    }

    override suspend fun sendPasswordReset(email: String) {
        requireAuth().sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun resendVerificationEmail() {
        val user = requireAuth().currentUser ?: error("Sign in before requesting verification")
        user.sendEmailVerification().await()
    }

    override suspend fun refreshEmailVerification(): Boolean {
        val user = requireAuth().currentUser ?: return false
        user.reload().await()
        return requireAuth().currentUser?.isEmailVerified == true
    }

    override suspend fun signOut() {
        requireAuth().signOut()
    }

    private fun requireAuth(): FirebaseAuth = auth
        ?: error("Firebase is not configured. Add app/google-services.json from the Firebase Console.")
}

private fun com.google.firebase.auth.FirebaseUser?.toDomain(): AuthenticatedUser? = this?.let {
    AuthenticatedUser(uid, email, displayName, isEmailVerified)
}
