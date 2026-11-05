package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.model.AuthenticatedUser

fun resolveAppRoute(user: AuthenticatedUser?, onboardingCompleted: Boolean): String = when {
    user == null || !user.emailVerified -> "auth"
    onboardingCompleted -> "home"
    else -> "onboarding"
}
