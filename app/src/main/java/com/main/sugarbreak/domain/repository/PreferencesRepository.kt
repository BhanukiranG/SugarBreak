package com.main.sugarbreak.domain.repository

import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ReminderSettings
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
