package com.main.sugarbreak.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.main.sugarbreak.data.local.converter.Converters
import com.main.sugarbreak.data.local.dao.ChallengeDao
import com.main.sugarbreak.data.local.dao.DailyCheckInDao
import com.main.sugarbreak.data.local.entity.ChallengeEntity
import com.main.sugarbreak.data.local.entity.DailyCheckInEntity
import com.main.sugarbreak.data.local.entity.PendingCheckInResetEntity

@Database(
    entities = [ChallengeEntity::class, DailyCheckInEntity::class, PendingCheckInResetEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SugarBreakDatabase : RoomDatabase() {
    abstract val challengeDao: ChallengeDao
    abstract val dailyCheckInDao: DailyCheckInDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE challenges ADD COLUMN ownerUid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE challenges ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE challenges SET syncId = 'legacy-challenge-' || id")
                db.execSQL("ALTER TABLE challenges ADD COLUMN updatedAt TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE challenges SET updatedAt = createdAt")
                db.execSQL("ALTER TABLE daily_check_ins ADD COLUMN ownerUid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE daily_check_ins ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE daily_check_ins SET syncId = 'legacy-checkin-' || id")
                db.execSQL("DROP INDEX IF EXISTS index_daily_check_ins_challengeId_date")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_check_ins_ownerUid_challengeId_date ON daily_check_ins(ownerUid, challengeId, date)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_challenges_ownerUid_syncId ON challenges(ownerUid, syncId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_check_ins_ownerUid_syncId ON daily_check_ins(ownerUid, syncId)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS pending_checkin_resets (" +
                        "ownerUid TEXT NOT NULL, " +
                        "challengeSyncId TEXT NOT NULL, " +
                        "requestedAt TEXT NOT NULL, " +
                        "PRIMARY KEY(ownerUid, challengeSyncId))"
                )
            }
        }
    }
}
