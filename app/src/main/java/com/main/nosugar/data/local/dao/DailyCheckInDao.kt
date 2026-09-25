package com.main.nosugar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.main.nosugar.data.local.entity.DailyCheckInEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyCheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkIn: DailyCheckInEntity): Long

    @Update
    suspend fun update(checkIn: DailyCheckInEntity)

    @Query("SELECT * FROM daily_check_ins WHERE challengeId = :challengeId AND date = :date LIMIT 1")
    suspend fun getCheckInForDate(challengeId: Long, date: LocalDate): DailyCheckInEntity?

    @Query("SELECT * FROM daily_check_ins WHERE challengeId = :challengeId ORDER BY date DESC")
    fun getCheckInsForChallenge(challengeId: Long): Flow<List<DailyCheckInEntity>>

    @Query("SELECT * FROM daily_check_ins WHERE challengeId = :challengeId ORDER BY date ASC")
    suspend fun getAllCheckInsForChallengeSync(challengeId: Long): List<DailyCheckInEntity>
}
