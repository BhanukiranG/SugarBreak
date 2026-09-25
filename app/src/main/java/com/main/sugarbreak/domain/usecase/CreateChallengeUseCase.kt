package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ChallengeStatus
import com.main.sugarbreak.domain.repository.ChallengeRepository
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
