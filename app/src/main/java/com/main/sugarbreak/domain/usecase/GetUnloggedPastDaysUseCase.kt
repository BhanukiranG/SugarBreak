package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetUnloggedPastDaysUseCase @Inject constructor(
    private val getActiveChallengeUseCase: GetActiveChallengeUseCase,
    private val checkInRepository: CheckInRepository
) {
    operator fun invoke(): Flow<List<LocalDate>> {
        return combine(
            getActiveChallengeUseCase(),
            checkInRepository.getAllCheckIns()
        ) { challenge, allCheckIns ->
            if (challenge == null) {
                return@combine emptyList()
            }

            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val startDate = maxOf(challenge.startDate, challenge.checkInResetDate ?: challenge.startDate)

            if (startDate.isAfter(yesterday)) {
                return@combine emptyList()
            }

            val completedDates = allCheckIns
                .filter { it.challengeId == challenge.id && it.status != CheckInStatus.PENDING }
                .map { it.date }
                .toSet()

            val unloggedDates = mutableListOf<LocalDate>()
            val totalDays = ChronoUnit.DAYS.between(startDate, yesterday)
            for (i in 0..totalDays) {
                val date = startDate.plusDays(i)
                if (date !in completedDates) {
                    unloggedDates.add(date)
                }
            }
            unloggedDates
        }
    }
}
