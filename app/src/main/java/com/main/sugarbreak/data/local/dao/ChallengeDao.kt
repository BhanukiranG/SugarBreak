package com.main.sugarbreak.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.main.sugarbreak.data.local.entity.ChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: ChallengeEntity): Long

    @Transaction
    suspend fun restoreSyncedChallenge(challenge: ChallengeEntity) {
        val current = getChallengeBySyncId(challenge.ownerUid, challenge.syncId)
        val newest = if (current != null && current.updatedAt > challenge.updatedAt) current else challenge
        val resetDate = listOfNotNull(current?.checkInResetDate, challenge.checkInResetDate).maxOrNull()
        val merged = newest.copy(id = current?.id ?: 0, checkInResetDate = resetDate)
        if (current != merged) insert(merged)
    }

    @Query("UPDATE challenges SET startDate = :startDate, targetSuccessfulDays = :targetSuccessfulDays, recoveryDays = :recoveryDays, status = :status, createdAt = :createdAt, behavior = :behavior, syncId = :syncId, updatedAt = :updatedAt WHERE id = :id AND ownerUid = :ownerUid")
    suspend fun update(
        id: Long,
        ownerUid: String,
        startDate: java.time.LocalDate,
        targetSuccessfulDays: Int,
        recoveryDays: Int,
        status: String,
        createdAt: java.time.LocalDateTime,
        behavior: String,
        syncId: String,
        updatedAt: java.time.LocalDateTime
    )

    @Query("SELECT * FROM challenges WHERE ownerUid = :ownerUid AND status = 'ACTIVE' LIMIT 1")
    fun getActiveChallenge(ownerUid: String): Flow<ChallengeEntity?>

    @Query("SELECT * FROM challenges WHERE id = :id AND ownerUid = :ownerUid")
    suspend fun getChallengeById(id: Long, ownerUid: String): ChallengeEntity?

    @Query("SELECT * FROM challenges WHERE ownerUid = :ownerUid")
    fun getAllChallenges(ownerUid: String): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE ownerUid = :ownerUid")
    suspend fun getAllChallengesSync(ownerUid: String): List<ChallengeEntity>

    @Query("SELECT * FROM challenges WHERE ownerUid = :ownerUid AND syncId = :syncId LIMIT 1")
    suspend fun getChallengeBySyncId(ownerUid: String, syncId: String): ChallengeEntity?

    @Query("UPDATE challenges SET ownerUid = :ownerUid WHERE ownerUid = ''")
    suspend fun claimLegacyChallenges(ownerUid: String)
}
