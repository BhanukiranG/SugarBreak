package com.main.sugarbreak.data.repository

import com.main.sugarbreak.data.local.dao.ChallengeDao
import com.main.sugarbreak.data.mapper.toDomain
import com.main.sugarbreak.data.mapper.toEntity
import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.repository.ChallengeRepository
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import com.main.sugarbreak.domain.repository.ProgressSyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class ChallengeRepositoryImpl @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val authenticationRepository: AuthenticationRepository,
    private val progressSyncRepository: ProgressSyncRepository
) : ChallengeRepository {
    override suspend fun insert(challenge: Challenge): Long {
        val uid = requireUid()
        val syncId = challenge.syncId.ifBlank { java.util.UUID.randomUUID().toString() }
        val id = challengeDao.insert(challenge.copy(ownerUid = uid, syncId = syncId).toEntity())
        progressSyncRepository.schedule(uid)
        return id
    }

    override suspend fun update(challenge: Challenge) {
        val uid = requireUid()
        val entity = challenge.copy(ownerUid = uid, updatedAt = LocalDateTime.now()).toEntity()
        challengeDao.update(
            entity.id, entity.ownerUid, entity.startDate, entity.targetSuccessfulDays, entity.recoveryDays,
            entity.status, entity.createdAt, entity.behavior, entity.syncId, entity.updatedAt
        )
        progressSyncRepository.schedule(uid)
    }

    override fun getActiveChallenge(): Flow<Challenge?> {
        return challengeDao.getActiveChallenge(ownerUidOrSignedOut()).map { it?.toDomain() }
    }

    override suspend fun getChallengeById(id: Long): Challenge? {
        return challengeDao.getChallengeById(id, ownerUidOrSignedOut())?.toDomain()
    }

    override fun getAllChallenges(): Flow<List<Challenge>> {
        return challengeDao.getAllChallenges(ownerUidOrSignedOut()).map { list -> list.map { it.toDomain() } }
    }

    private fun requireUid(): String = authenticationRepository.currentUserSnapshot()?.uid
        ?: error("An authenticated account is required to save progress")

    private fun ownerUidOrSignedOut(): String = authenticationRepository.currentUserSnapshot()?.uid
        ?: "__signed_out__"
}
