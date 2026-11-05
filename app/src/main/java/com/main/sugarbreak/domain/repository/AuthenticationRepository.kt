package com.main.sugarbreak.domain.repository

import com.main.sugarbreak.domain.model.AuthenticatedUser
import kotlinx.coroutines.flow.Flow

interface AuthenticationRepository {
    val currentUser: Flow<AuthenticatedUser?>
    fun currentUserSnapshot(): AuthenticatedUser?
    suspend fun registerWithEmail(email: String, password: String)
    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signInWithGoogleIdToken(idToken: String)
    suspend fun sendPasswordReset(email: String)
    suspend fun resendVerificationEmail()
    suspend fun refreshEmailVerification(): Boolean
    suspend fun signOut()
}
