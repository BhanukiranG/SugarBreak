package com.main.sugarbreak.domain.model

data class AuthenticatedUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val emailVerified: Boolean
)
