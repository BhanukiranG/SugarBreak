package com.main.sugarbreak.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.main.sugarbreak.data.local.entity.ChallengeEntity
import com.main.sugarbreak.data.local.entity.DailyCheckInEntity
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingCheckInResetInstrumentedTest {
    private lateinit var database: SugarBreakDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SugarBreakDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun resetQueuesCloudDeletionAndOnlyClearsSignedInUsersCheckIns() = runBlocking {
        val date = LocalDate.of(2026, 1, 10)
        val timestamp = LocalDateTime.of(2026, 1, 10, 9, 0)
        database.challengeDao.insert(
            ChallengeEntity(
                startDate = date,
                targetSuccessfulDays = 30,
                recoveryDays = 0,
                status = "ACTIVE",
                createdAt = timestamp,
                behavior = "CONTINUE",
                ownerUid = "user-a",
                syncId = "challenge-a",
                updatedAt = timestamp
            )
        )
        database.dailyCheckInDao.insert(
            DailyCheckInEntity(
                challengeId = 1,
                date = date,
                status = "SUCCESS",
                reason = null,
                createdAt = timestamp,
                updatedAt = timestamp,
                ownerUid = "user-a",
                syncId = "challenge-a_2026-01-10"
            )
        )
        database.dailyCheckInDao.insert(
            DailyCheckInEntity(
                challengeId = 1,
                date = date,
                status = "SUCCESS",
                reason = null,
                createdAt = timestamp,
                updatedAt = timestamp,
                ownerUid = "user-b",
                syncId = "challenge-b_2026-01-10"
            )
        )

        database.dailyCheckInDao.resetCheckInsForChallenge(
            challengeId = 1,
            ownerUid = "user-a",
            challengeSyncId = "challenge-a"
        )

        assertEquals(emptyList<DailyCheckInEntity>(), database.dailyCheckInDao.getAllCheckInsSync("user-a"))
        assertEquals(1, database.dailyCheckInDao.getAllCheckInsSync("user-b").size)
        val pending = database.dailyCheckInDao.getPendingChallengeResets("user-a")
        assertEquals(listOf("challenge-a"), pending.map { it.challengeSyncId })
        assertEquals(emptyList<com.main.sugarbreak.data.local.entity.PendingCheckInResetEntity>(),
            database.dailyCheckInDao.getPendingChallengeResets("user-b"))
    }
}
