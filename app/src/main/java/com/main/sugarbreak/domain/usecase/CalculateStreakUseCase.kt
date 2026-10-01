package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.StreakSummary
import com.main.sugarbreak.domain.repository.ChallengeRepository
import com.main.sugarbreak.domain.repository.CheckInRepository
import com.main.sugarbreak.util.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalculateStreakUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val challengeRepository: ChallengeRepository
) {
    operator fun invoke(challengeId: Long): Flow<StreakSummary> {
        return checkInRepository.getCheckInsForChallenge(challengeId).map { checkIns ->
            val challenge = challengeRepository.getChallengeById(challengeId)
            val behavior = challenge?.behavior ?: ChallengeBehavior.RESET_STREAK
            StreakCalculator.calculate(checkIns, challengeBehavior = behavior)
        }
    }
}
