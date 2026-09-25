package com.main.nosugar.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.nosugar.domain.model.ChallengeBehavior
import com.main.nosugar.domain.repository.PreferencesRepository
import com.main.nosugar.domain.usecase.CreateChallengeUseCase
import com.main.nosugar.domain.usecase.ScheduleReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentStep: Int = 1,
    val selectedRule: ChallengeBehavior = ChallengeBehavior.CONTINUE, // Using CONTINUE as default behavior for UI purposes
    val targetDays: Int = 30,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val userName: String = "",
    val onboardingCompleted: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val createChallengeUseCase: CreateChallengeUseCase,
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun nextStep() {
        _state.update { 
            if (it.currentStep < 6) it.copy(currentStep = it.currentStep + 1) else it 
        }
    }

    fun previousStep() {
        _state.update { 
            if (it.currentStep > 1) it.copy(currentStep = it.currentStep - 1) else it 
        }
    }

    fun setRule(behavior: ChallengeBehavior) {
        _state.update { it.copy(selectedRule = behavior) }
    }

    fun setTargetDays(days: Int) {
        _state.update { it.copy(targetDays = days) }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _state.update { it.copy(reminderHour = hour, reminderMinute = minute) }
    }

    fun setUserName(name: String) {
        _state.update { it.copy(userName = name) }
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            val currentState = _state.value
            createChallengeUseCase(
                targetSuccessfulDays = currentState.targetDays,
                behavior = currentState.selectedRule
            )
            scheduleReminderUseCase(
                hour = currentState.reminderHour,
                minute = currentState.reminderMinute
            )
            preferencesRepository.setOnboardingCompleted(true)
            preferencesRepository.updateChallengeBehavior(currentState.selectedRule)
            if (currentState.userName.isNotBlank()) {
                preferencesRepository.setUserName(currentState.userName)
            }
            _state.update { it.copy(onboardingCompleted = true) }
        }
    }
}
