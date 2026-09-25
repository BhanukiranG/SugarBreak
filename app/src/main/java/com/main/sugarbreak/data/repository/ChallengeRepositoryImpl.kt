package com.main.sugarbreak.data.repository

import com.main.sugarbreak.data.local.dao.ChallengeDao
import com.main.sugarbreak.data.mapper.toDomain
import com.main.sugarbreak.data.mapper.toEntity
import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChallengeRepositoryImpl @Inject constructor(
    private val challengeDao: ChallengeDao
) : ChallengeRepository {
    override suspend fun insert(challenge: Challenge): Long {
        return challengeDao.insert(challenge.toEntity())
    }

    override suspend fun update(challenge: Challenge) {
        challengeDao.update(challenge.toEntity())
    }

    override fun getActiveChallenge(): Flow<Challenge?> {
        return challengeDao.getActiveChallenge().map { it?.toDomain() }
    }

    override suspend fun getChallengeById(id: Long): Challenge? {
        return challengeDao.getChallengeById(id)?.toDomain()
    }

    override fun getAllChallenges(): Flow<List<Challenge>> {
        return challengeDao.getAllChallenges().map { list -> list.map { it.toDomain() } }
    }
}
