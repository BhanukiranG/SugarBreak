package com.main.sugarbreak.data.repository

import com.main.sugarbreak.data.preferences.PreferencesManager
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ReminderSettings
import com.main.sugarbreak.domain.repository.PreferencesRepository
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import com.main.sugarbreak.domain.repository.ProgressSyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val authenticationRepository: AuthenticationRepository,
    private val progressSyncRepository: ProgressSyncRepository
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getChallengeBehavior(): Flow<ChallengeBehavior> {
        return authenticationRepository.currentUser.flatMapLatest { user ->
            preferencesManager.challengeBehavior(user?.uid).map { stored ->
                stored?.value?.let { behaviorStr ->
                    runCatching { ChallengeBehavior.valueOf(behaviorStr) }.getOrDefault(ChallengeBehavior.CONTINUE)
                } ?: ChallengeBehavior.CONTINUE
            }
        }
    }

    override suspend fun updateChallengeBehavior(behavior: ChallengeBehavior) {
        val uid = authenticationRepository.currentUserSnapshot()?.uid
            ?: error("An authenticated account is required to save challenge behavior")
        preferencesManager.setChallengeBehavior(uid, behavior.name)
        progressSyncRepository.schedule(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getOnboardingCompleted(): Flow<Boolean> {
        return authenticationRepository.currentUser.flatMapLatest { user ->
            preferencesManager.onboardingCompleted(user?.uid).map { it ?: false }
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        val uid = authenticationRepository.currentUserSnapshot()?.uid
            ?: error("An authenticated account is required to save onboarding state")
        preferencesManager.setOnboardingCompleted(uid, completed)
        progressSyncRepository.schedule(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserName(): Flow<String> {
        return authenticationRepository.currentUser.flatMapLatest { user ->
            preferencesManager.userName(user?.uid).map {
                if (it?.value.isNullOrBlank() || it?.value == "Local Profile") "Friend" else it!!.value
            }
        }
    }

    override suspend fun setUserName(name: String) {
        val uid = authenticationRepository.currentUserSnapshot()?.uid
            ?: error("An authenticated account is required to save a profile")
        preferencesManager.setUserName(uid, name)
        progressSyncRepository.schedule(uid)
    }

    override fun getIsDarkMode(): Flow<Boolean?> {
        return preferencesManager.isDarkMode
    }

    override suspend fun setIsDarkMode(isDark: Boolean?) {
        preferencesManager.setIsDarkMode(isDark)
    }

    override fun getAppTheme(): Flow<com.main.sugarbreak.domain.model.AppTheme> {
        return preferencesManager.appTheme.map { themeStr ->
            themeStr?.let { 
                try {
                    com.main.sugarbreak.domain.model.AppTheme.valueOf(it) 
                } catch (e: Exception) {
                    com.main.sugarbreak.domain.model.AppTheme.MINT
                }
            } ?: com.main.sugarbreak.domain.model.AppTheme.MINT
        }
    }

    override suspend fun setAppTheme(theme: com.main.sugarbreak.domain.model.AppTheme) {
        preferencesManager.setAppTheme(theme.name)
    }
}


