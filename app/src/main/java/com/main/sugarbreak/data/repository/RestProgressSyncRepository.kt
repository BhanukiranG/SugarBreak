package com.main.sugarbreak.data.repository

import androidx.room.withTransaction
import com.main.sugarbreak.data.local.RestSyncState
import com.main.sugarbreak.data.local.SugarBreakDatabase
import com.main.sugarbreak.data.local.entity.ChallengeEntity
import com.main.sugarbreak.data.local.entity.DailyCheckInEntity
import com.main.sugarbreak.data.preferences.PreferencesManager
import com.main.sugarbreak.domain.repository.ProgressSyncRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestProgressSyncRepository @Inject constructor(
    private val database: SugarBreakDatabase,
    private val auth: RestAuthenticationRepository,
    private val preferences: PreferencesManager
) : ProgressSyncRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val requests = Channel<String>(Channel.CONFLATED)
    private val mutex = Mutex()
    private val challenges get() = database.challengeDao
    private val checks get() = database.dailyCheckInDao
    private val zone get() = ZoneId.systemDefault()

    init {
        scope.launch {
            for (uid in requests) {
                var backoff = 1_000L
                while (auth.currentUserSnapshot()?.uid == uid) {
                    try {
                        syncNow(uid)
                        break
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        // Let a new account or a new request interrupt a failed retry.
                        val next = withTimeoutOrNull(backoff) { requests.receive() }
                        if (next != null && next != uid) {
                            requests.trySend(next)
                            break
                        }
                        backoff = (backoff * 2).coerceAtMost(60_000)
                    }
                }
            }
        }
    }

    override fun schedule(uid: String) { requests.trySend(uid) }

    override suspend fun syncNow(uid: String) = mutex.withLock {
        check(auth.currentUserSnapshot()?.uid == uid)
        syncProfile(uid)
        val state = database.restSyncDao.read(uid)?.let { JSONObject(it.state) } ?: JSONObject()
        val versions = state.optJSONObject("versions") ?: JSONObject().also { state.put("versions", it) }
        val fingerprints = state.optJSONObject("fingerprints") ?: JSONObject().also { state.put("fingerprints", it) }
        val outbox = state.optJSONArray("outbox") ?: database.withTransaction {
            collectMutations(uid, versions, fingerprints).also {
                state.put("outbox", it)
                persist(uid, state)
            }
        }

        // Upload in order, then pull the canonical page stream. Retain the exact outbox
        // until its results and cursor have committed to Room.
        val canonical = linkedMapOf<String, JSONObject>()
        val acceptedFingerprints = mutableMapOf<String, String>()
        val acceptedResets = mutableSetOf<String>()
        var cursor = state.optLong("cursor")
        for (i in 0 until outbox.length()) {
            val mutation = outbox.getJSONObject(i)
            val result = exchange(uid, cursor, JSONArray().put(mutation))
            val outcome = result.getJSONArray("results").getJSONObject(0)
            val record = outcome.optJSONObject("record")
            if (record != null) canonical[record.getString("id")] = record
            if (outcome.getBoolean("accepted") && mutation.getString("kind") != "reset") {
                acceptedFingerprints[mutation.getString("id")] = mutation.getJSONObject("payload").toString()
            } else if (outcome.getBoolean("accepted")) {
                acceptedResets.add(mutation.getString("id"))
            }
        }
        do {
            val page = exchange(uid, cursor, JSONArray())
            val records = page.getJSONArray("records")
            for (i in 0 until records.length()) {
                val record = records.getJSONObject(i)
                canonical[record.getString("id")] = record
            }
            cursor = page.getLong("cursor")
            val more = page.getBoolean("hasMore")
            if (!more) break
        } while (true)

        database.withTransaction {
            check(auth.currentUserSnapshot()?.uid == uid)
            val queuedPayloads = (0 until outbox.length()).associate { index ->
                val item = outbox.getJSONObject(index)
                item.getString("id") to item.optJSONObject("payload")?.toString()
            }
            val resets = checks.getPendingChallengeResets(uid).associateBy { it.challengeSyncId }
            val completedResets = (0 until outbox.length()).map { outbox.getJSONObject(it) }
                .filter { it.getString("kind") == "reset" }
                .associate { it.getString("id") to it.getString("requestedAt") }
            // Parents can have newer versions than their children after a dashboard edit.
            canonical.values.sortedBy { if (it.getString("kind") == "challenge") 0 else 1 }.forEach { record ->
                val id = record.getString("id")
                val payload = JSONObject(record.getString("payload"))
                val previous = fingerprints.optString(id)
                val queued = queuedPayloads[id]
                if (record.getString("kind") == "challenge") {
                    val local = challenges.getChallengeBySyncId(uid, id)
                    val localPayload = local?.let { challengePayload(it).toString() }
                    val editedWhileSyncing = localPayload != null && localPayload != previous &&
                        localPayload != queued && localPayload != acceptedFingerprints[id]
                    if (!editedWhileSyncing) {
                        val resetDate = record.optString("resetAt").takeUnless { it.isBlank() || it == "null" }
                            ?.let { OffsetDateTime.parse(it).atZoneSameInstant(zone).toLocalDate() }
                        challenges.insert(ChallengeEntity(
                            id = local?.id ?: 0,
                            startDate = LocalDate.parse(payload.getString("startDate")),
                            targetSuccessfulDays = payload.getInt("targetSuccessfulDays"),
                            recoveryDays = payload.optInt("recoveryDays"),
                            status = payload.optString("status", "ACTIVE"),
                            createdAt = localTime(payload.getString("createdAt")),
                            behavior = payload.getString("behavior"),
                            ownerUid = uid,
                            syncId = id,
                            updatedAt = localTime(payload.optString("updatedAt", payload.getString("createdAt"))),
                            checkInResetDate = maxOfNullable(local?.checkInResetDate, resetDate)
                        ))
                    }
                } else {
                    val parentId = record.getString("challengeId")
                    val parent = challenges.getChallengeBySyncId(uid, parentId) ?: error("Missing synced challenge")
                    val day = LocalDate.parse(record.getString("day"))
                    val local = checks.getCheckInForDate(parent.id, day, uid)
                    val localPayload = local?.let { checkInPayload(it).toString() }
                    val editedWhileSyncing = localPayload != null && localPayload != previous && localPayload != queued
                    val localReset = resets[parentId]
                    val createdAt = localTime(payload.getString("createdAt"))
                    if (!editedWhileSyncing && (localReset == null || createdAt.isAfter(localReset.requestedAt))) {
                        if (record.getBoolean("deleted")) {
                            if (local != null) checks.deleteCheckInsByIds(uid, listOf(local.id))
                        } else {
                            checks.insert(DailyCheckInEntity(
                                id = local?.id ?: 0, challengeId = parent.id, date = day,
                                status = payload.getString("status"),
                                reason = payload.optString("reason").takeUnless { it.isBlank() || it == "null" },
                                createdAt = createdAt,
                                updatedAt = localTime(payload.optString("updatedAt", payload.getString("createdAt"))),
                                ownerUid = uid, syncId = "${parentId}_$day"
                            ))
                        }
                    }
                }
                versions.put(id, record.getLong("version"))
                fingerprints.put(id, if (record.getBoolean("deleted")) "" else normalizedPayload(record.getString("kind"), payload))
            }
            completedResets.forEach { (id, timestamp) ->
                val reset = resets[id]
                if (id in acceptedResets && reset?.requestedAt?.toString() == timestamp) {
                    checks.removePendingChallengeReset(uid, id, reset.requestedAt)
                }
            }
            state.put("cursor", cursor)
            state.remove("outbox")
            persist(uid, state)
        }
        if (outbox.length() > 0) schedule(uid)
    }

    private suspend fun collectMutations(uid: String, versions: JSONObject, fingerprints: JSONObject): JSONArray {
        val result = JSONArray()
        val localChallenges = challenges.getAllChallengesSync(uid)
        fun append(id: String, kind: String, payload: JSONObject, parent: String? = null, day: String? = null) {
            if (payload.toString() != fingerprints.optString(id)) {
                result.put(mutation(id, kind, versions.optLong(id), payload).apply {
                    parent?.let { put("challengeId", it) }
                    day?.let { put("day", it) }
                })
            }
        }
        val resets = checks.getPendingChallengeResets(uid).associateBy { it.challengeSyncId }
        for (challenge in localChallenges) {
            val id = challenge.syncId
            val reset = resets[id]
            if (reset != null && versions.optLong(id) > 0) {
                result.put(mutation(id, "reset", versions.getLong(id), JSONObject())
                    .put("resetCutoff", instant(reset.requestedAt))
                    .put("requestedAt", reset.requestedAt.toString()))
                continue
            }
            append(id, "challenge", challengePayload(challenge))
            if (reset != null) continue
            for (row in checks.getAllCheckInsForChallengeSync(challenge.id, uid)) {
                append(checkInId(id, row.date), "checkIn", checkInPayload(row), id, row.date.toString())
            }
        }
        return result
    }

    private fun mutation(id: String, kind: String, version: Long, payload: JSONObject) =
        JSONObject().put("mutationId", UUID.randomUUID().toString()).put("id", id)
            .put("kind", kind).put("expectedVersion", version).put("payload", payload)

    private fun challengePayload(c: ChallengeEntity) = JSONObject()
        .put("startDate", c.startDate.toString()).put("targetSuccessfulDays", c.targetSuccessfulDays)
        .put("behavior", c.behavior).put("status", c.status).put("recoveryDays", c.recoveryDays)
        .put("createdAt", instant(c.createdAt)).put("updatedAt", instant(c.updatedAt))

    private fun checkInPayload(c: DailyCheckInEntity) = JSONObject().put("status", c.status)
        .put("reason", c.reason ?: JSONObject.NULL).put("createdAt", instant(c.createdAt)).put("updatedAt", instant(c.updatedAt))

    private fun normalizedPayload(kind: String, payload: JSONObject): String {
        val keys = if (kind == "challenge") listOf("startDate", "targetSuccessfulDays", "behavior", "status", "recoveryDays", "createdAt", "updatedAt")
                   else listOf("status", "reason", "createdAt", "updatedAt")
        return JSONObject().apply {
            keys.forEach { key ->
                when {
                    key == "createdAt" || key == "updatedAt" ->
                        put(key, OffsetDateTime.parse(payload.getString(key)).toInstant().toString())
                    key == "recoveryDays" -> put(key, payload.optInt(key))
                    key == "reason" -> put(key, payload.opt(key) ?: JSONObject.NULL)
                    payload.has(key) -> put(key, payload.get(key))
                }
            }
        }.toString()
    }

    private fun instant(local: LocalDateTime) = local.atZone(zone).toInstant().toString()
    private fun localTime(value: String) = OffsetDateTime.parse(value).atZoneSameInstant(zone).toLocalDateTime()
    private fun maxOfNullable(a: LocalDate?, b: LocalDate?) = listOfNotNull(a, b).maxOrNull()
    private fun checkInId(parent: String, day: LocalDate) =
        UUID.nameUUIDFromBytes("$parent/$day".encodeToByteArray()).toString()

    private suspend fun persist(uid: String, state: JSONObject) =
        database.restSyncDao.save(RestSyncState(uid, state.toString()))

    private suspend fun exchange(uid: String, cursor: Long, changes: JSONArray) = JSONObject(auth.request(
        uid, "POST", "/me/progress", JSONObject().put("cursor", cursor).put("mutations", changes)
            .toString().encodeToByteArray()
    ).decodeToString())

    private suspend fun syncProfile(uid: String) {
        val state = database.restSyncDao.read("$uid/profile")?.let { JSONObject(it.state) }
        val remote = JSONObject(auth.request(uid, "GET", "/me").decodeToString())
        val name = preferences.userName(uid).first()?.value.orEmpty()
        val behavior = preferences.challengeBehavior(uid).first()?.value ?: "CONTINUE"
        val onboarded = preferences.onboardingCompleted(uid).first() ?: false
        val dirty = if (state == null) name.isNotBlank() && remote.optLong("version") == 0L
        else name != state.optString("displayName") ||
            behavior != state.optString("challengeBehavior") || onboarded != state.optBoolean("onboardingCompleted")
        val final = if (dirty && (state?.optLong("version") ?: 0L) == remote.optLong("version") && name.isNotBlank()) {
            val body = JSONObject().put("displayName", name).put("challengeBehavior", behavior)
                .put("version", remote.getLong("version")).put("onboardingCompleted", onboarded)
            try {
                JSONObject(auth.request(uid, "PUT", "/me", body.toString().encodeToByteArray()).decodeToString())
            } catch (conflict: ApiError) {
                if (conflict.status != 409) throw conflict
                JSONObject(auth.request(uid, "GET", "/me").decodeToString())
            }
        } else remote
        if (preferences.userName(uid).first()?.value.orEmpty() == name &&
            preferences.challengeBehavior(uid).first()?.value.orEmpty().ifEmpty { "CONTINUE" } == behavior &&
            (preferences.onboardingCompleted(uid).first() ?: false) == onboarded) {
            preferences.setUserName(uid, final.optString("displayName"))
            preferences.setChallengeBehavior(uid, final.getString("challengeBehavior"))
            preferences.setOnboardingCompleted(uid, final.getBoolean("onboardingCompleted"))
        }
        persist("$uid/profile", final)
    }
}
