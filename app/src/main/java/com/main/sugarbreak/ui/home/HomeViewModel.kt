package com.main.sugarbreak.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.model.StreakSummary
import com.main.sugarbreak.domain.usecase.CalculateStreakUseCase
import com.main.sugarbreak.domain.usecase.GetActiveChallengeUseCase
import com.main.sugarbreak.domain.usecase.GetTodayCheckInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeState(
    val activeChallenge: Challenge? = null,
    val streakSummary: StreakSummary? = null,
    val todayCheckIn: DailyCheckIn? = null,
    val userName: String = "Local Profile",
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getActiveChallengeUseCase: GetActiveChallengeUseCase,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val getTodayCheckInUseCase: GetTodayCheckInUseCase,
    private val preferencesRepository: com.main.sugarbreak.domain.repository.PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        combine(
            getActiveChallengeUseCase(),
            preferencesRepository.getUserName()
        ) { challenge, userName ->
            Pair(challenge, userName)
        }.onEach { (challenge, userName) ->
            if (challenge != null) {
                combine(
                    calculateStreakUseCase(challenge.id),
                    getTodayCheckInUseCase(challenge.id)
                ) { streak, checkIn ->
                    _state.update {
                        it.copy(
                            activeChallenge = challenge,
                            streakSummary = streak,
                            todayCheckIn = checkIn,
                            userName = userName,
                            isLoading = false
                        )
                    }
                }.launchIn(viewModelScope)
            } else {
                _state.update { it.copy(activeChallenge = null, userName = userName, isLoading = false) }
            }
        }.launchIn(viewModelScope)
    }
}
