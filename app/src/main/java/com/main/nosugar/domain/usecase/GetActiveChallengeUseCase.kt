package com.main.nosugar.domain.usecase

import com.main.nosugar.domain.model.Challenge
import com.main.nosugar.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveChallengeUseCase @Inject constructor(
    private val challengeRepository: ChallengeRepository
) {
    operator fun invoke(): Flow<Challenge?> {
        return challengeRepository.getActiveChallenge()
    }
}
