package com.main.sugarbreak.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.main.sugarbreak.data.local.entity.DailyCheckInEntity
import com.main.sugarbreak.data.local.entity.PendingCheckInResetEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface DailyCheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkIn: DailyCheckInEntity): Long

    @Query("UPDATE daily_check_ins SET status = :status, reason = :reason, updatedAt = :updatedAt, syncId = :syncId WHERE id = :id AND ownerUid = :ownerUid AND challengeId = :challengeId")
    suspend fun update(
        id: Long,
        ownerUid: String,
        challengeId: Long,
        status: String,
        reason: String?,
        updatedAt: java.time.LocalDateTime,
        syncId: String
    )

    @Query("SELECT * FROM daily_check_ins WHERE ownerUid = :ownerUid AND challengeId = :challengeId AND date = :date LIMIT 1")
    suspend fun getCheckInForDate(challengeId: Long, date: LocalDate, ownerUid: String): DailyCheckInEntity?

    @Query("SELECT * FROM daily_check_ins WHERE ownerUid = :ownerUid AND challengeId = :challengeId ORDER BY date DESC")
    fun getCheckInsForChallenge(challengeId: Long, ownerUid: String): Flow<List<DailyCheckInEntity>>

    @Query("SELECT * FROM daily_check_ins WHERE ownerUid = :ownerUid AND challengeId = :challengeId ORDER BY date ASC")
    suspend fun getAllCheckInsForChallengeSync(challengeId: Long, ownerUid: String): List<DailyCheckInEntity>

    @Query("DELETE FROM daily_check_ins WHERE ownerUid = :ownerUid AND challengeId = :challengeId")
    suspend fun deleteAllCheckInsForChallenge(challengeId: Long, ownerUid: String)

    @Query("DELETE FROM daily_check_ins WHERE ownerUid = :ownerUid AND id IN (:ids)")
    suspend fun deleteCheckInsByIds(ownerUid: String, ids: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueChallengeReset(reset: PendingCheckInResetEntity)

    @Query("SELECT * FROM pending_checkin_resets WHERE ownerUid = :ownerUid")
    suspend fun getPendingChallengeResets(ownerUid: String): List<PendingCheckInResetEntity>

    @Query("DELETE FROM pending_checkin_resets WHERE ownerUid = :ownerUid AND challengeSyncId = :challengeSyncId AND requestedAt = :requestedAt")
    suspend fun removePendingChallengeReset(ownerUid: String, challengeSyncId: String, requestedAt: LocalDateTime)

    @Query("SELECT * FROM pending_checkin_resets WHERE ownerUid = :ownerUid AND challengeSyncId = :challengeSyncId")
    suspend fun getPendingChallengeReset(ownerUid: String, challengeSyncId: String): PendingCheckInResetEntity?

    @Transaction
    suspend fun restoreSyncedCheckIn(checkIn: DailyCheckInEntity, challengeSyncId: String): Boolean {
        val reset = getPendingChallengeReset(checkIn.ownerUid, challengeSyncId)
        if (reset != null && !checkIn.createdAt.isAfter(reset.requestedAt)) return false
        val current = getCheckInForDate(checkIn.challengeId, checkIn.date, checkIn.ownerUid)
        if (current != null && current.updatedAt.isAfter(checkIn.updatedAt)) return false
        val updated = checkIn.copy(id = current?.id ?: 0)
        if (current != updated) insert(updated)
        return true
    }

    @Transaction
    suspend fun resetCheckInsForChallenge(
        challengeId: Long,
        ownerUid: String,
        challengeSyncId: String
    ) {
        val requestedAt = LocalDateTime.now()
        recordCheckInResetDate(challengeId, ownerUid, requestedAt.toLocalDate())
        enqueueChallengeReset(
            PendingCheckInResetEntity(
                ownerUid = ownerUid,
                challengeSyncId = challengeSyncId,
                requestedAt = requestedAt
            )
        )
        deleteAllCheckInsForChallenge(challengeId, ownerUid)
    }

    @Query("UPDATE challenges SET checkInResetDate = :date WHERE id = :challengeId AND ownerUid = :ownerUid")
    suspend fun recordCheckInResetDate(challengeId: Long, ownerUid: String, date: LocalDate)

    @Query("SELECT * FROM daily_check_ins WHERE ownerUid = :ownerUid AND date = :date AND status != 'PENDING' LIMIT 1")
    suspend fun getAnyCompletedCheckInForDate(date: LocalDate, ownerUid: String): DailyCheckInEntity?

    @Query("SELECT * FROM daily_check_ins WHERE ownerUid = :ownerUid ORDER BY date DESC")
    fun getAllCheckIns(ownerUid: String): Flow<List<DailyCheckInEntity>>

    @Query("SELECT * FROM daily_check_ins WHERE ownerUid = :ownerUid")
    suspend fun getAllCheckInsSync(ownerUid: String): List<DailyCheckInEntity>

    @Query("SELECT * FROM daily_check_ins WHERE ownerUid = :ownerUid AND syncId = :syncId LIMIT 1")
    suspend fun getCheckInBySyncId(ownerUid: String, syncId: String): DailyCheckInEntity?

    @Query("UPDATE daily_check_ins SET ownerUid = :ownerUid WHERE ownerUid = ''")
    suspend fun claimLegacyCheckIns(ownerUid: String)
}
