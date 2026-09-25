package com.main.sugarbreak.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.main.sugarbreak.data.local.converter.Converters
import com.main.sugarbreak.data.local.dao.ChallengeDao
import com.main.sugarbreak.data.local.dao.DailyCheckInDao
import com.main.sugarbreak.data.local.entity.ChallengeEntity
import com.main.sugarbreak.data.local.entity.DailyCheckInEntity

@Database(
    entities = [ChallengeEntity::class, DailyCheckInEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SugarBreakDatabase : RoomDatabase() {
    abstract val challengeDao: ChallengeDao
    abstract val dailyCheckInDao: DailyCheckInDao
}
