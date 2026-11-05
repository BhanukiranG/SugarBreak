package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.repository.AuthenticationRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke() = authenticationRepository.signOut()
}
