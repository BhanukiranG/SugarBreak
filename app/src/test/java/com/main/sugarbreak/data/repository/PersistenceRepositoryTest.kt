package com.main.sugarbreak.data.repository

import com.main.sugarbreak.data.local.dao.ChallengeDao
import com.main.sugarbreak.data.local.dao.DailyCheckInDao
import com.main.sugarbreak.data.local.entity.ChallengeEntity
import com.main.sugarbreak.data.local.entity.DailyCheckInEntity
import com.main.sugarbreak.data.local.entity.PendingCheckInResetEntity
import com.main.sugarbreak.domain.model.AuthenticatedUser
import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ChallengeStatus
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.model.SlipReason
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import com.main.sugarbreak.domain.repository.ProgressSyncRepository
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistenceRepositoryTest {
    private class FakeAuthenticationRepository(private val uid: String = "account-a") : AuthenticationRepository {
        override val currentUser: Flow<AuthenticatedUser?> = flowOf(AuthenticatedUser(uid, null, null, true))
        override fun currentUserSnapshot() = AuthenticatedUser(uid, null, null, true)
        override suspend fun registerWithEmail(email: String, password: String) = Unit
        override suspend fun signInWithEmail(email: String, password: String) = Unit
        override suspend fun signInWithGoogleIdToken(idToken: String) = Unit
        override suspend fun sendPasswordReset(email: String) = Unit
        override suspend fun resendVerificationEmail() = Unit
        override suspend fun refreshEmailVerification() = true
        override suspend fun signOut() = Unit
    }

    private class FakeProgressSyncRepository : ProgressSyncRepository {
        val scheduledUids = mutableListOf<String>()
        override suspend fun syncNow(uid: String) = Unit
        override fun schedule(uid: String) { scheduledUids += uid }
    }

    private class FakeChallengeDao : ChallengeDao {
        var inserted: ChallengeEntity? = null
        var updated: ChallengeEntity? = null
        var existing: ChallengeEntity? = null
        override suspend fun insert(challenge: ChallengeEntity): Long {
            inserted = challenge
            return 42L
        }
        override suspend fun update(
            id: Long,
            ownerUid: String,
            startDate: LocalDate,
            targetSuccessfulDays: Int,
            recoveryDays: Int,
            status: String,
            createdAt: LocalDateTime,
            behavior: String,
            syncId: String,
            updatedAt: LocalDateTime
        ) {
            updated = ChallengeEntity(
                id, startDate, targetSuccessfulDays, recoveryDays, status, createdAt,
                behavior, ownerUid, syncId, updatedAt
            )
        }
        override fun getActiveChallenge(ownerUid: String): Flow<ChallengeEntity?> = flowOf(existing)
        override suspend fun getChallengeById(id: Long, ownerUid: String): ChallengeEntity? =
            existing?.takeIf { it.id == id && it.ownerUid == ownerUid }
        override fun getAllChallenges(ownerUid: String): Flow<List<ChallengeEntity>> = flowOf(emptyList())
        override suspend fun getAllChallengesSync(ownerUid: String): List<ChallengeEntity> = emptyList()
        override suspend fun getChallengeBySyncId(ownerUid: String, syncId: String): ChallengeEntity? = null
        override suspend fun claimLegacyChallenges(ownerUid: String) = Unit
    }

    private class FakeDailyCheckInDao : DailyCheckInDao {
        var inserted: DailyCheckInEntity? = null
        var updated: DailyCheckInEntity? = null
        var reset: Triple<Long, String, String>? = null
        var pendingReset: PendingCheckInResetEntity? = null
        override suspend fun insert(checkIn: DailyCheckInEntity): Long {
            inserted = checkIn
            return 5L
        }
        override suspend fun update(
            id: Long,
            ownerUid: String,
            challengeId: Long,
            status: String,
            reason: String?,
            updatedAt: LocalDateTime,
            syncId: String
        ) {
            updated = DailyCheckInEntity(
                id, challengeId, LocalDate.of(2026, 1, 10), status, reason,
                LocalDateTime.of(2026, 1, 10, 8, 0), updatedAt, ownerUid, syncId
            )
        }
        override suspend fun getCheckInForDate(challengeId: Long, date: LocalDate, ownerUid: String): DailyCheckInEntity? = null
        override fun getCheckInsForChallenge(challengeId: Long, ownerUid: String): Flow<List<DailyCheckInEntity>> = flowOf(emptyList())
        override suspend fun getAllCheckInsForChallengeSync(challengeId: Long, ownerUid: String): List<DailyCheckInEntity> = emptyList()
        override suspend fun deleteAllCheckInsForChallenge(challengeId: Long, ownerUid: String) = Unit
        override suspend fun deleteCheckInsByIds(ownerUid: String, ids: List<Long>) = Unit
        override suspend fun enqueueChallengeReset(reset: PendingCheckInResetEntity) = Unit
        override suspend fun recordCheckInResetDate(challengeId: Long, ownerUid: String, date: LocalDate) = Unit
        override suspend fun getPendingChallengeResets(ownerUid: String): List<PendingCheckInResetEntity> = emptyList()
        override suspend fun removePendingChallengeReset(ownerUid: String, challengeSyncId: String, requestedAt: LocalDateTime) = Unit
        override suspend fun getPendingChallengeReset(ownerUid: String, challengeSyncId: String) =
            pendingReset?.takeIf { it.ownerUid == ownerUid && it.challengeSyncId == challengeSyncId }
        override suspend fun resetCheckInsForChallenge(challengeId: Long, ownerUid: String, challengeSyncId: String) {
            reset = Triple(challengeId, ownerUid, challengeSyncId)
        }
        override suspend fun getAnyCompletedCheckInForDate(date: LocalDate, ownerUid: String): DailyCheckInEntity? = null
        override fun getAllCheckIns(ownerUid: String): Flow<List<DailyCheckInEntity>> = flowOf(emptyList())
        override suspend fun getAllCheckInsSync(ownerUid: String): List<DailyCheckInEntity> = emptyList()
        override suspend fun getCheckInBySyncId(ownerUid: String, syncId: String): DailyCheckInEntity? = null
        override suspend fun claimLegacyCheckIns(ownerUid: String) = Unit
    }

    private val timestamp = LocalDateTime.of(2026, 1, 10, 9, 0)

    private fun sampleChallenge(id: Long = 0) = Challenge(
        id = id,
        startDate = timestamp.toLocalDate(),
        targetSuccessfulDays = 30,
        recoveryDays = 0,
        status = ChallengeStatus.ACTIVE,
        createdAt = timestamp,
        behavior = ChallengeBehavior.CONTINUE
    )

    private fun localChallenge() = ChallengeEntity(
        id = 42,
        startDate = timestamp.toLocalDate(),
        targetSuccessfulDays = 30,
        recoveryDays = 0,
        status = "ACTIVE",
        createdAt = timestamp,
        behavior = "CONTINUE",
        ownerUid = "account-a",
        syncId = "challenge-stable-id",
        updatedAt = timestamp
    )

    @Test
    fun challengeInsertAssignsUidStableIdAndSchedulesCloudSync() = runBlocking {
        val dao = FakeChallengeDao()
        val sync = FakeProgressSyncRepository()
        val repository = ChallengeRepositoryImpl(dao, FakeAuthenticationRepository(), sync)

        assertEquals(42L, repository.insert(sampleChallenge()))

        assertEquals("account-a", dao.inserted?.ownerUid)
        assertTrue(dao.inserted?.syncId?.isNotBlank() == true)
        assertEquals(listOf("account-a"), sync.scheduledUids)
    }

    @Test
    fun challengeUpdatePersistsChangedFieldsAndSchedulesCloudSync() = runBlocking {
        val dao = FakeChallengeDao()
        val sync = FakeProgressSyncRepository()
        val repository = ChallengeRepositoryImpl(dao, FakeAuthenticationRepository(), sync)

        repository.update(sampleChallenge(42).copy(targetSuccessfulDays = 45))

        assertEquals("account-a", dao.updated?.ownerUid)
        assertEquals(45, dao.updated?.targetSuccessfulDays)
        assertTrue(dao.updated!!.updatedAt.isAfter(timestamp))
        assertEquals(listOf("account-a"), sync.scheduledUids)
    }

    @Test
    fun checkInInsertPersistsUidAndStableChallengeDateIdAndSchedulesSync() = runBlocking {
        val challengeDao = FakeChallengeDao().apply { existing = localChallenge() }
        val checkInDao = FakeDailyCheckInDao()
        val sync = FakeProgressSyncRepository()
        val repository = CheckInRepositoryImpl(checkInDao, challengeDao, FakeAuthenticationRepository(), sync)
        val date = timestamp.toLocalDate()

        repository.insert(
            DailyCheckIn(0, 42, date, CheckInStatus.SUCCESS, null, timestamp, timestamp)
        )

        assertEquals("account-a", checkInDao.inserted?.ownerUid)
        assertEquals("challenge-stable-id_$date", checkInDao.inserted?.syncId)
        assertEquals(listOf("account-a"), sync.scheduledUids)
    }

    @Test
    fun checkInResetQueuesAccountScopedCloudResetBeforeScheduling() = runBlocking {
        val challengeDao = FakeChallengeDao().apply { existing = localChallenge() }
        val checkInDao = FakeDailyCheckInDao()
        val sync = FakeProgressSyncRepository()
        val repository = CheckInRepositoryImpl(checkInDao, challengeDao, FakeAuthenticationRepository(), sync)

        repository.deleteAllCheckInsForChallenge(42)

        assertEquals(Triple(42L, "account-a", "challenge-stable-id"), checkInDao.reset)
        assertEquals(listOf("account-a"), sync.scheduledUids)
    }

    @Test
    fun syncCannotRestorePreResetCheckInButCanRestoreANewCheckInForTheSameDay() = runBlocking {
        val dao = FakeDailyCheckInDao()
        val old = DailyCheckInEntity(
            challengeId = 42, date = timestamp.toLocalDate(), status = "SUCCESS", reason = null,
            createdAt = timestamp, updatedAt = timestamp, ownerUid = "account-a"
        )
        // Sync took its snapshot before the user tapped reset.
        val resetAt = timestamp.plusMinutes(1)
        dao.pendingReset = PendingCheckInResetEntity(old.ownerUid, "challenge-stable-id", resetAt)
        assertEquals(false, dao.restoreSyncedCheckIn(old, "challenge-stable-id"))
        assertEquals(null, dao.inserted)

        val new = old.copy(createdAt = resetAt.plusSeconds(1), updatedAt = resetAt.plusSeconds(1))
        assertTrue(dao.restoreSyncedCheckIn(new, "challenge-stable-id"))
        assertEquals(new, dao.inserted)
    }
}
