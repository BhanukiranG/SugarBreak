package com.main.sugarbreak.domain.repository

import com.main.sugarbreak.domain.model.Challenge
import kotlinx.coroutines.flow.Flow

interface ChallengeRepository {
    suspend fun insert(challenge: Challenge): Long
    suspend fun update(challenge: Challenge)
    fun getActiveChallenge(): Flow<Challenge?>
    suspend fun getChallengeById(id: Long): Challenge?
    fun getAllChallenges(): Flow<List<Challenge>>
}
