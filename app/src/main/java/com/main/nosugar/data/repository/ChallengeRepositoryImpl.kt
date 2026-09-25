package com.main.nosugar.data.repository

import com.main.nosugar.data.local.dao.ChallengeDao
import com.main.nosugar.data.mapper.toDomain
import com.main.nosugar.data.mapper.toEntity
import com.main.nosugar.domain.model.Challenge
import com.main.nosugar.domain.repository.ChallengeRepository
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
