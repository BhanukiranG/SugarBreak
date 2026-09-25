package com.main.nosugar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.main.nosugar.data.local.converter.Converters
import com.main.nosugar.data.local.dao.ChallengeDao
import com.main.nosugar.data.local.dao.DailyCheckInDao
import com.main.nosugar.data.local.entity.ChallengeEntity
import com.main.nosugar.data.local.entity.DailyCheckInEntity

@Database(
    entities = [ChallengeEntity::class, DailyCheckInEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NoSugarDatabase : RoomDatabase() {
    abstract val challengeDao: ChallengeDao
    abstract val dailyCheckInDao: DailyCheckInDao
}
