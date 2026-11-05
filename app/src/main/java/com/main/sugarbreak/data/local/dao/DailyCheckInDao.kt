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

    @Query("DELETE FROM pending_checkin_resets WHERE ownerUid = :ownerUid AND challengeSyncId = :challengeSyncId")
    suspend fun removePendingChallengeReset(ownerUid: String, challengeSyncId: String)

    @Transaction
    suspend fun resetCheckInsForChallenge(
        challengeId: Long,
        ownerUid: String,
        challengeSyncId: String
    ) {
        enqueueChallengeReset(
            PendingCheckInResetEntity(
                ownerUid = ownerUid,
                challengeSyncId = challengeSyncId,
                requestedAt = java.time.LocalDateTime.now()
            )
        )
        deleteAllCheckInsForChallenge(challengeId, ownerUid)
    }

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
