package com.main.nosugar.domain.repository

import com.main.nosugar.domain.model.ChallengeBehavior
import com.main.nosugar.domain.model.ReminderSettings
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getReminderSettings(): Flow<ReminderSettings>
    suspend fun updateReminderSettings(settings: ReminderSettings)
    fun getChallengeBehavior(): Flow<ChallengeBehavior>
    suspend fun updateChallengeBehavior(behavior: ChallengeBehavior)
    fun getOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
    fun getUserName(): Flow<String>
    suspend fun setUserName(name: String)
}
