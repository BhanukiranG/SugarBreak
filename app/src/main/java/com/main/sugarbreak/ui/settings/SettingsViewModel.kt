package com.main.sugarbreak.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ReminderSettings
import com.main.sugarbreak.domain.repository.ChallengeRepository
import com.main.sugarbreak.domain.repository.CheckInRepository
import com.main.sugarbreak.domain.repository.PreferencesRepository
import com.main.sugarbreak.domain.usecase.CancelReminderUseCase
import com.main.sugarbreak.domain.usecase.GetActiveChallengeUseCase
import com.main.sugarbreak.domain.usecase.ScheduleReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class SettingsUiState(
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime = LocalTime.of(20, 0),
    val trackingRule: String = "Strict",
    val challengeBehavior: ChallengeBehavior = ChallengeBehavior.CONTINUE,
    val userName: String = "Local Profile",
    val challengeGoalDays: Int = 30,
    val activeChallengeId: Long? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val getActiveChallengeUseCase: GetActiveChallengeUseCase,
    private val challengeRepository: ChallengeRepository,
    private val checkInRepository: CheckInRepository,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val cancelReminderUseCase: CancelReminderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesRepository.getReminderSettings(),
                preferencesRepository.getChallengeBehavior(),
                preferencesRepository.getUserName(),
                getActiveChallengeUseCase()
            ) { reminderSettings, behavior, userName, challenge ->
                SettingsTuple(reminderSettings, behavior, userName, challenge)
            }.collectLatest { tuple ->
                _uiState.update { 
                    it.copy(
                        reminderEnabled = tuple.reminderSettings.enabled,
                        reminderTime = LocalTime.of(tuple.reminderSettings.hour, tuple.reminderSettings.minute),
                        challengeBehavior = tuple.behavior,
                        userName = tuple.userName,
                        challengeGoalDays = tuple.challenge?.targetSuccessfulDays ?: 30,
                        activeChallengeId = tuple.challenge?.id,
                        isLoading = false
                    ) 
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value
            preferencesRepository.updateReminderSettings(
                ReminderSettings(enabled, current.reminderTime.hour, current.reminderTime.minute)
            )
            if (enabled) {
                scheduleReminderUseCase(current.reminderTime.hour, current.reminderTime.minute)
            } else {
                cancelReminderUseCase()
            }
        }
    }

    fun updateReminderTime(time: LocalTime) {
        viewModelScope.launch {
            val current = _uiState.value
            preferencesRepository.updateReminderSettings(
                ReminderSettings(current.reminderEnabled, time.hour, time.minute)
            )
            if (current.reminderEnabled) {
                scheduleReminderUseCase(time.hour, time.minute)
            }
        }
    }

    fun updateChallengeBehavior(behavior: ChallengeBehavior) {
        viewModelScope.launch {
            preferencesRepository.updateChallengeBehavior(behavior)
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferencesRepository.setUserName(name)
        }
    }

    fun updateChallengeGoal(targetDays: Int) {
        viewModelScope.launch {
            val currentChallenge = getActiveChallengeUseCase().firstOrNull()
            if (currentChallenge != null) {
                val updated = currentChallenge.copy(targetSuccessfulDays = targetDays)
                challengeRepository.update(updated)
            }
        }
    }

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            val activeChallengeId = _uiState.value.activeChallengeId
            if (activeChallengeId != null) {
                checkInRepository.deleteAllCheckInsForChallenge(activeChallengeId)
            }
            onComplete()
        }
    }

    fun exportDataCsv(context: Context) {
        viewModelScope.launch {
            val activeChallengeId = _uiState.value.activeChallengeId
            if (activeChallengeId != null) {
                val records = checkInRepository.getAllCheckInsForChallengeSync(activeChallengeId)
                val csvBuilder = StringBuilder()
                csvBuilder.append("Date,Status,Reason\n")
                records.forEach { record ->
                    val reasonText = record.reason?.name ?: "None"
                    csvBuilder.append("${record.date},${record.status},$reasonText\n")
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "SugarBreak Tracking Data")
                    putExtra(Intent.EXTRA_TEXT, csvBuilder.toString())
                }
                val chooser = Intent.createChooser(intent, "Export Tracking Data")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        }
    }
}

private data class SettingsTuple(
    val reminderSettings: ReminderSettings,
    val behavior: ChallengeBehavior,
    val userName: String,
    val challenge: Challenge?
)

    fun setIsDarkMode(isDark: Boolean?) {
        viewModelScope.launch {
            preferencesRepository.setIsDarkMode(isDark)
        }
    }

