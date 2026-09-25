package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveChallengeUseCase @Inject constructor(
    private val challengeRepository: ChallengeRepository
) {
    operator fun invoke(): Flow<Challenge?> {
        return challengeRepository.getActiveChallenge()
    }
}
