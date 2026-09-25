package com.main.nosugar.domain.usecase

import com.main.nosugar.domain.model.Challenge
import com.main.nosugar.domain.model.ChallengeBehavior
import com.main.nosugar.domain.model.ChallengeStatus
import com.main.nosugar.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class CreateChallengeUseCase @Inject constructor(
    private val challengeRepository: ChallengeRepository
) {
    suspend operator fun invoke(
        targetSuccessfulDays: Int,
        behavior: ChallengeBehavior
    ) {
        val activeChallenge = challengeRepository.getActiveChallenge().firstOrNull()
        if (activeChallenge != null) {
            challengeRepository.update(
                activeChallenge.copy(status = ChallengeStatus.CANCELLED)
            )
        }
        
        val newChallenge = Challenge(
            id = 0,
            startDate = LocalDate.now(),
            targetSuccessfulDays = targetSuccessfulDays,
            recoveryDays = 0,
            status = ChallengeStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            behavior = behavior
        )
        challengeRepository.insert(newChallenge)
    }
}
