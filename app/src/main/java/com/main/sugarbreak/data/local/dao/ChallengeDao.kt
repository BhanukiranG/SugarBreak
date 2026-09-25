package com.main.sugarbreak.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.main.sugarbreak.data.local.entity.ChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: ChallengeEntity): Long

    @Update
    suspend fun update(challenge: ChallengeEntity)

    @Query("SELECT * FROM challenges WHERE status = 'ACTIVE' LIMIT 1")
    fun getActiveChallenge(): Flow<ChallengeEntity?>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getChallengeById(id: Long): ChallengeEntity?

    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>
}
