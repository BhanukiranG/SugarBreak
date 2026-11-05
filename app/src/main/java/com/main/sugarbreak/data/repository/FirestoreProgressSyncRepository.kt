package com.main.sugarbreak.data.repository

import com.main.sugarbreak.data.firebase.FirebaseServices
import com.main.sugarbreak.data.local.dao.ChallengeDao
import com.main.sugarbreak.data.local.dao.DailyCheckInDao
import com.main.sugarbreak.data.local.entity.ChallengeEntity
import com.main.sugarbreak.data.local.entity.DailyCheckInEntity
import com.main.sugarbreak.data.preferences.PreferencesManager
import com.main.sugarbreak.domain.repository.ProgressSyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreProgressSyncRepository @Inject constructor(
    private val services: FirebaseServices,
    private val challengeDao: ChallengeDao,
    private val checkInDao: DailyCheckInDao,
    private val preferencesManager: PreferencesManager
) : ProgressSyncRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncMutex = Mutex()

    override fun schedule(uid: String) {
        scope.launch {
            runCatching { syncNow(uid) }
        }
    }

    override suspend fun syncNow(uid: String) {
        syncMutex.withLock { syncInternal(uid) }
    }

    private suspend fun syncInternal(uid: String) {
        val firestore = services.firestore ?: return
        challengeDao.claimLegacyChallenges(uid)
        checkInDao.claimLegacyCheckIns(uid)
        preferencesManager.claimLegacyUserName(uid)

        val user = firestore.collection("users").document(uid)
        val localChallenges = challengeDao.getAllChallengesSync(uid)
        val remoteChallenges = user.collection("challenges").get().await().documents.mapNotNull {
            it.toChallenge(uid)
        }
        val newestChallenges = mergeByNewest(
            localChallenges,
            remoteChallenges,
            keyOf = { it.syncId },
            updatedAt = { it.updatedAt }
        )
        val mergedChallenges = newestChallenges.map { remoteOrLocal ->
            val existing = localChallenges.firstOrNull { it.syncId == remoteOrLocal.syncId }
            remoteOrLocal.copy(id = existing?.id ?: 0, ownerUid = uid)
        }
        mergedChallenges.forEach { challenge ->
            if (localChallenges.firstOrNull { it.syncId == challenge.syncId } != challenge) {
                challengeDao.insert(challenge)
            }
        }
        val savedChallenges = challengeDao.getAllChallengesSync(uid)
        val localChecksRaw = checkInDao.getAllCheckInsSync(uid)
        val localChecks = localChecksRaw.map { record ->
            val challengeSyncId = savedChallenges.firstOrNull { it.id == record.challengeId }?.syncId.orEmpty()
            record.copy(syncId = "${challengeSyncId}_${record.date}")
        }
        val remoteChecks = user.collection("checkIns").get().await().documents.mapNotNull { doc ->
            doc.toCheckIn(uid, savedChallenges)
        }
        val mergedChecks = mergeByNewest(
            localChecks,
            remoteChecks,
            keyOf = { it.syncId },
            updatedAt = { it.updatedAt }
        )
            .map { candidate ->
                val existing = localChecks.firstOrNull { it.syncId == candidate.syncId }
                candidate.copy(id = existing?.id ?: 0, ownerUid = uid)
            }
        mergedChecks.forEach { checkIn ->
            val existing = localChecksRaw.firstOrNull { it.id == checkIn.id }
            if (existing != checkIn) checkInDao.insert(checkIn)
        }

        val remoteName = user.get().await().getString("displayName")
        val currentName = preferencesManager.userName(uid).first()
        val displayName = when {
            currentName.isNullOrBlank() || currentName == "Local Profile" -> remoteName.orEmpty()
            else -> currentName
        }
        if (!remoteName.isNullOrBlank() && (currentName.isNullOrBlank() || currentName == "Local Profile")) {
            preferencesManager.setUserName(uid, remoteName)
        }

        val writes = mutableListOf<Pair<com.google.firebase.firestore.DocumentReference, Map<String, Any?>>>()
        writes += user to mapOf("displayName" to displayName, "updatedAt" to System.currentTimeMillis())
        mergedChallenges.filter { challenge ->
            val remote = remoteChallenges.firstOrNull { it.syncId == challenge.syncId }
            val local = localChallenges.firstOrNull { it.syncId == challenge.syncId }
            remote == null || (local != null && local.updatedAt >= remote.updatedAt)
        }.forEach { challenge ->
            writes += user.collection("challenges").document(challenge.syncId) to challenge.toCloudMap()
        }
        mergedChecks.filter { checkIn ->
            val remote = remoteChecks.firstOrNull { it.syncId == checkIn.syncId }
            val local = localChecks.firstOrNull { it.syncId == checkIn.syncId }
            remote == null || (local != null && local.updatedAt >= remote.updatedAt)
        }.forEach { checkIn ->
            writes += user.collection("checkIns").document(checkIn.syncId) to checkIn.toCloudMap(savedChallenges)
        }
        writes.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { (reference, data) -> batch.set(reference, data) }
            batch.commit().await()
        }
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toChallenge(uid: String): ChallengeEntity? = runCatching {
    val created = LocalDateTime.parse(getString("createdAt"))
    ChallengeEntity(
        id = 0,
        startDate = LocalDate.parse(getString("startDate")),
        targetSuccessfulDays = getLong("targetSuccessfulDays")?.toInt() ?: return null,
        recoveryDays = getLong("recoveryDays")?.toInt() ?: 0,
        status = getString("status") ?: return null,
        createdAt = created,
        behavior = getString("behavior") ?: return null,
        ownerUid = uid,
        syncId = getString("syncId") ?: id,
        updatedAt = getString("updatedAt")?.let(LocalDateTime::parse) ?: created
    )
}.getOrNull()

private fun com.google.firebase.firestore.DocumentSnapshot.toCheckIn(
    uid: String,
    challenges: List<ChallengeEntity>
): DailyCheckInEntity? = runCatching {
    val challengeSyncId = getString("challengeSyncId") ?: return null
    val challenge = challenges.firstOrNull { it.syncId == challengeSyncId } ?: return null
    DailyCheckInEntity(
        id = 0,
        challengeId = challenge.id,
        date = LocalDate.parse(getString("date")),
        status = getString("status") ?: return null,
        reason = getString("reason"),
        createdAt = LocalDateTime.parse(getString("createdAt")),
        updatedAt = LocalDateTime.parse(getString("updatedAt")),
        ownerUid = uid,
        syncId = getString("syncId") ?: id
    )
}.getOrNull()

private fun ChallengeEntity.toCloudMap(): Map<String, Any> = mapOf(
    "syncId" to syncId,
    "startDate" to startDate.toString(),
    "targetSuccessfulDays" to targetSuccessfulDays,
    "recoveryDays" to recoveryDays,
    "status" to status,
    "createdAt" to createdAt.toString(),
    "updatedAt" to updatedAt.toString(),
    "behavior" to behavior
)

private fun DailyCheckInEntity.toCloudMap(challenges: List<ChallengeEntity>): Map<String, Any?> = mapOf(
    "syncId" to syncId,
    "challengeSyncId" to challenges.firstOrNull { it.id == challengeId }?.syncId,
    "date" to date.toString(),
    "status" to status,
    "reason" to reason,
    "createdAt" to createdAt.toString(),
    "updatedAt" to updatedAt.toString()
)
