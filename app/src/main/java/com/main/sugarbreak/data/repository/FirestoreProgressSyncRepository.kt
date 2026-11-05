package com.main.sugarbreak.data.repository

import com.main.sugarbreak.data.firebase.FirebaseServices
import com.main.sugarbreak.data.local.dao.ChallengeDao
import com.main.sugarbreak.data.local.dao.DailyCheckInDao
import com.main.sugarbreak.data.local.entity.ChallengeEntity
import com.main.sugarbreak.data.local.entity.DailyCheckInEntity
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.data.preferences.PreferencesManager
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import com.main.sugarbreak.domain.repository.ProgressSyncRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreProgressSyncRepository @Inject constructor(
    private val services: FirebaseServices,
    private val challengeDao: ChallengeDao,
    private val checkInDao: DailyCheckInDao,
    private val preferencesManager: PreferencesManager,
    private val authenticationRepository: AuthenticationRepository
) : ProgressSyncRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncMutex = Mutex()
    private val syncRequests = ConcurrentHashMap<String, Channel<Unit>>()

    override fun schedule(uid: String) {
        val requests = syncRequests.computeIfAbsent(uid) { accountUid ->
            Channel<Unit>(Channel.CONFLATED).also { channel ->
                scope.launch {
                    for (ignored in channel) {
                        var retryDelay = 1_000L
                        while (authenticationRepository.currentUserSnapshot()?.uid == accountUid) {
                            try {
                                syncNow(accountUid)
                                break
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (_: Exception) {
                                delay(retryDelay)
                                retryDelay = (retryDelay * 2).coerceAtMost(60_000L)
                            }
                        }
                    }
                }
            }
        }
        requests.trySend(Unit)
    }

    override suspend fun syncNow(uid: String) {
        syncMutex.withLock { syncInternal(uid) }
    }

    private suspend fun syncInternal(uid: String) {
        preferencesManager.claimLegacyOnboarding(uid)
        preferencesManager.claimLegacyChallengeBehavior(uid)
        val firestore = services.firestore ?: return
        challengeDao.claimLegacyChallenges(uid)
        checkInDao.claimLegacyCheckIns(uid)
        preferencesManager.claimLegacyUserName(uid)

        val user = firestore.collection("users").document(uid)
        val remoteProfile = user.get().await()
        val localChallenges = challengeDao.getAllChallengesSync(uid)
        val remoteChallengeSnapshot = user.collection("challenges").get().await()
        val remoteChallenges = remoteChallengeSnapshot.documents.mapNotNull {
            it.toChallenge(uid)
        }
        val localOnboarding = preferencesManager.onboardingCompleted(uid).first()
        val onboardingCompleted = resolveOnboardingCompletion(
            localCompletion = localOnboarding,
            remoteCompletion = remoteProfile.getBoolean("onboardingCompleted"),
            hasRemoteChallenges = !remoteChallengeSnapshot.isEmpty
        )
        preferencesManager.setOnboardingCompleted(uid, onboardingCompleted)
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
        val pendingResets = checkInDao.getPendingChallengeResets(uid)
        val pendingResetCutoffs = pendingResets.associate { it.challengeSyncId to it.requestedAt }
        pendingResets.forEach { reset ->
            val localChallenge = savedChallenges.firstOrNull { it.syncId == reset.challengeSyncId } ?: return@forEach
            val staleCheckInIds = checkInDao.getAllCheckInsForChallengeSync(localChallenge.id, uid)
                .filter { !it.createdAt.isAfter(reset.requestedAt) }
                .map { it.id }
            staleCheckInIds.chunked(900).forEach { ids -> checkInDao.deleteCheckInsByIds(uid, ids) }
        }
        val localChecksRaw = checkInDao.getAllCheckInsSync(uid)
        val localChecks = localChecksRaw.map { record ->
            val challengeSyncId = savedChallenges.firstOrNull { it.id == record.challengeId }?.syncId.orEmpty()
            record.copy(syncId = "${challengeSyncId}_${record.date}")
        }
        val remoteCheckInSnapshot = user.collection("checkIns").get().await()
        val resetDocuments = remoteCheckInSnapshot.documents.filter { document ->
            isCheckInSuppressedByPendingReset(
                challengeSyncId = document.getString("challengeSyncId"),
                createdAt = document.getString("createdAt")?.let { raw ->
                    runCatching { LocalDateTime.parse(raw) }.getOrNull()
                },
                pendingResetCutoffs = pendingResetCutoffs
            )
        }
        resetDocuments.chunked(400).forEach { documents ->
            val deletionBatch = firestore.batch()
            documents.forEach { deletionBatch.delete(it.reference) }
            deletionBatch.commit().await()
        }
        if (!remoteCheckInSnapshot.metadata.isFromCache) {
            pendingResets.forEach { reset ->
                checkInDao.removePendingChallengeReset(uid, reset.challengeSyncId)
            }
        }
        val remoteChecks = remoteCheckInSnapshot.documents
            .filterNot {
                isCheckInSuppressedByPendingReset(
                    challengeSyncId = it.getString("challengeSyncId"),
                    createdAt = it.getString("createdAt")?.let { raw ->
                        runCatching { LocalDateTime.parse(raw) }.getOrNull()
                    },
                    pendingResetCutoffs = pendingResetCutoffs
                )
            }
            .mapNotNull { doc -> doc.toCheckIn(uid, savedChallenges) }

        val localBehavior = preferencesManager.challengeBehavior(uid).first()
        val remoteBehavior = remoteProfile.getString("challengeBehavior")
            ?.takeIf { raw -> runCatching { ChallengeBehavior.valueOf(raw) }.isSuccess }
        val remoteBehaviorUpdatedAt = remoteProfile.getLong("challengeBehaviorUpdatedAt") ?: 0L
        val resolvedBehavior = resolveChallengeBehavior(
            localValue = localBehavior?.value,
            localUpdatedAt = localBehavior?.updatedAt ?: 0L,
            remoteValue = remoteBehavior,
            remoteUpdatedAt = remoteBehaviorUpdatedAt
        )
        resolvedBehavior?.let { (value, updatedAt) ->
            preferencesManager.setChallengeBehavior(uid, value, updatedAt)
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

        val localName = preferencesManager.userName(uid).first()
        val resolvedName = resolveTimestampedValue(
            localValue = localName?.value?.takeUnless { it == "Local Profile" },
            localUpdatedAt = localName?.updatedAt ?: 0L,
            remoteValue = remoteProfile.getString("displayName")?.takeUnless { it.isBlank() },
            remoteUpdatedAt = remoteProfile.getLong("displayNameUpdatedAt") ?: 0L
        )
        resolvedName?.let { (value, updatedAt) -> preferencesManager.setUserName(uid, value, updatedAt) }

        val writes = mutableListOf<Pair<com.google.firebase.firestore.DocumentReference, Map<String, Any?>>>()
        writes += user to mapOf(
            "displayName" to resolvedName?.first.orEmpty(),
            "displayNameUpdatedAt" to resolvedName?.second,
            "onboardingCompleted" to onboardingCompleted,
            "challengeBehavior" to resolvedBehavior?.first,
            "challengeBehaviorUpdatedAt" to resolvedBehavior?.second,
            "updatedAt" to System.currentTimeMillis()
        )
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
