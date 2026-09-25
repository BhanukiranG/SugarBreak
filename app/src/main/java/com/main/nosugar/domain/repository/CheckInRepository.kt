package com.main.nosugar.domain.repository

import com.main.nosugar.domain.model.DailyCheckIn
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CheckInRepository {
    suspend fun insert(checkIn: DailyCheckIn)
    suspend fun update(checkIn: DailyCheckIn)
    suspend fun getCheckInForDate(challengeId: Long, date: LocalDate): DailyCheckIn?
    fun getCheckInsForChallenge(challengeId: Long): Flow<List<DailyCheckIn>>
    suspend fun getAllCheckInsForChallengeSync(challengeId: Long): List<DailyCheckIn>
}
