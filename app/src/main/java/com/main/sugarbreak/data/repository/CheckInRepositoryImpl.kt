package com.main.sugarbreak.data.repository

import com.main.sugarbreak.data.local.dao.DailyCheckInDao
import com.main.sugarbreak.data.local.dao.ChallengeDao
import com.main.sugarbreak.data.mapper.toDomain
import com.main.sugarbreak.data.mapper.toEntity
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.repository.CheckInRepository
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import com.main.sugarbreak.domain.repository.ProgressSyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class CheckInRepositoryImpl @Inject constructor(
    private val dailyCheckInDao: DailyCheckInDao,
    private val challengeDao: ChallengeDao,
    private val authenticationRepository: AuthenticationRepository,
    private val progressSyncRepository: ProgressSyncRepository
) : CheckInRepository {
    override suspend fun insert(checkIn: DailyCheckIn) {
        val entity = checkIn.toEntity().forCurrentUser(checkIn)
        dailyCheckInDao.insert(entity)
        progressSyncRepository.schedule(entity.ownerUid)
    }

    override suspend fun update(checkIn: DailyCheckIn) {
        val entity = checkIn.toEntity().forCurrentUser(checkIn)
        dailyCheckInDao.update(
            entity.id, entity.ownerUid, entity.challengeId, entity.status, entity.reason,
            entity.updatedAt, entity.syncId
        )
        progressSyncRepository.schedule(entity.ownerUid)
    }

    override suspend fun getCheckInForDate(challengeId: Long, date: LocalDate): DailyCheckIn? {
        return dailyCheckInDao.getCheckInForDate(challengeId, date, ownerUidOrSignedOut())?.toDomain()
    }

    override fun getCheckInsForChallenge(challengeId: Long): Flow<List<DailyCheckIn>> {
        return dailyCheckInDao.getCheckInsForChallenge(challengeId, ownerUidOrSignedOut()).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAllCheckInsForChallengeSync(challengeId: Long): List<DailyCheckIn> {
        return dailyCheckInDao.getAllCheckInsForChallengeSync(challengeId, ownerUidOrSignedOut()).map { it.toDomain() }
    }

    override suspend fun deleteAllCheckInsForChallenge(challengeId: Long) {
        val uid = authenticationRepository.currentUserSnapshot()?.uid
            ?: error("An authenticated account is required to reset progress")
        val challenge = challengeDao.getChallengeById(challengeId, uid)
            ?: error("Challenge not found for the signed-in account")
        dailyCheckInDao.resetCheckInsForChallenge(challengeId, uid, challenge.syncId)
        progressSyncRepository.schedule(uid)
    }

    override suspend fun hasCompletedCheckInForDate(date: LocalDate): Boolean {
        return dailyCheckInDao.getAnyCompletedCheckInForDate(date, ownerUidOrSignedOut()) != null
    }

    override fun getAllCheckIns(): Flow<List<DailyCheckIn>> {
        return dailyCheckInDao.getAllCheckIns(ownerUidOrSignedOut()).map { list -> list.map { it.toDomain() } }
    }

    private suspend fun com.main.sugarbreak.data.local.entity.DailyCheckInEntity.forCurrentUser(
        checkIn: DailyCheckIn
    ): com.main.sugarbreak.data.local.entity.DailyCheckInEntity {
        val uid = authenticationRepository.currentUserSnapshot()?.uid
            ?: error("An authenticated account is required to save progress")
        val challenge = challengeDao.getChallengeById(checkIn.challengeId, uid)
            ?: error("Challenge not found for the signed-in account")
        return copy(ownerUid = uid, syncId = "${challenge.syncId}_${checkIn.date}")
    }

    private fun ownerUidOrSignedOut(): String = authenticationRepository.currentUserSnapshot()?.uid
        ?: "__signed_out__"
}
