package com.main.nosugar.data.repository

import com.main.nosugar.data.preferences.PreferencesManager
import com.main.nosugar.domain.model.ChallengeBehavior
import com.main.nosugar.domain.model.ReminderSettings
import com.main.nosugar.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager
) : PreferencesRepository {
    
    override fun getReminderSettings(): Flow<ReminderSettings> {
        return combine(
            preferencesManager.reminderEnabled,
            preferencesManager.reminderHour,
            preferencesManager.reminderMinute
        ) { enabled, hour, minute ->
            ReminderSettings(enabled, hour, minute)
        }
    }

    override suspend fun updateReminderSettings(settings: ReminderSettings) {
        preferencesManager.setReminderEnabled(settings.enabled)
        preferencesManager.setReminderTime(settings.hour, settings.minute)
    }

    override fun getChallengeBehavior(): Flow<ChallengeBehavior> {
        return preferencesManager.challengeBehavior.map { behaviorStr ->
            behaviorStr?.let { ChallengeBehavior.valueOf(it) } ?: ChallengeBehavior.CONTINUE
        }
    }

    override suspend fun updateChallengeBehavior(behavior: ChallengeBehavior) {
        preferencesManager.setChallengeBehavior(behavior.name)
    }

    override fun getOnboardingCompleted(): Flow<Boolean> {
        return preferencesManager.onboardingCompleted
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesManager.setOnboardingCompleted(completed)
    }

    override fun getUserName(): Flow<String> {
        return preferencesManager.userName.map { it ?: "Local Profile" }
    }

    override suspend fun setUserName(name: String) {
        preferencesManager.setUserName(name)
    }
}
