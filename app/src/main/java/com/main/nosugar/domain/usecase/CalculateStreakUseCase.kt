package com.main.nosugar.domain.usecase

import com.main.nosugar.domain.model.StreakSummary
import com.main.nosugar.domain.repository.CheckInRepository
import com.main.nosugar.util.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalculateStreakUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository
) {
    operator fun invoke(challengeId: Long): Flow<StreakSummary> {
        return checkInRepository.getCheckInsForChallenge(challengeId).map { checkIns ->
            StreakCalculator.calculate(checkIns)
        }
    }
}
