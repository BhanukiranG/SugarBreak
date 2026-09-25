package com.main.nosugar.data.repository

import com.main.nosugar.data.local.dao.DailyCheckInDao
import com.main.nosugar.data.mapper.toDomain
import com.main.nosugar.data.mapper.toEntity
import com.main.nosugar.domain.model.DailyCheckIn
import com.main.nosugar.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class CheckInRepositoryImpl @Inject constructor(
    private val dailyCheckInDao: DailyCheckInDao
) : CheckInRepository {
    override suspend fun insert(checkIn: DailyCheckIn) {
        dailyCheckInDao.insert(checkIn.toEntity())
    }

    override suspend fun update(checkIn: DailyCheckIn) {
        dailyCheckInDao.update(checkIn.toEntity())
    }

    override suspend fun getCheckInForDate(challengeId: Long, date: LocalDate): DailyCheckIn? {
        return dailyCheckInDao.getCheckInForDate(challengeId, date)?.toDomain()
    }

    override fun getCheckInsForChallenge(challengeId: Long): Flow<List<DailyCheckIn>> {
        return dailyCheckInDao.getCheckInsForChallenge(challengeId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAllCheckInsForChallengeSync(challengeId: Long): List<DailyCheckIn> {
        return dailyCheckInDao.getAllCheckInsForChallengeSync(challengeId).map { it.toDomain() }
    }
}
