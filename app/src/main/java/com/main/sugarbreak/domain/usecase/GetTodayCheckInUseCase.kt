package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetTodayCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository
) {
    operator fun invoke(challengeId: Long): Flow<DailyCheckIn?> {
        return checkInRepository.getCheckInsForChallenge(challengeId).map { checkIns ->
            checkIns.find { it.date == LocalDate.now() }
        }
    }
}
