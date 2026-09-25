package com.main.sugarbreak.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ReminderSettings
import com.main.sugarbreak.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.main.sugarbreak.domain.usecase.ScheduleReminderUseCase
import com.main.sugarbreak.domain.usecase.CancelReminderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalTime

data class SettingsUiState(
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime = LocalTime.of(20, 0),
    val trackingRule: String = "Strict",
    val challengeBehavior: ChallengeBehavior = ChallengeBehavior.CONTINUE,
    val userName: String = "Local Profile",
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
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
                preferencesRepository.getUserName()
            ) { reminderSettings, behavior, userName ->
                Triple(reminderSettings, behavior, userName)
            }.collectLatest { (reminderSettings, behavior, userName) ->
                _uiState.update { 
                    it.copy(
                        reminderEnabled = reminderSettings.enabled,
                        reminderTime = LocalTime.of(reminderSettings.hour, reminderSettings.minute),
                        challengeBehavior = behavior,
                        userName = userName,
                        isLoading = false
                    ) 
                }
            }
        }
    }

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
}
