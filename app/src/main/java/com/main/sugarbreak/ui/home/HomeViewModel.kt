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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val activeChallenge: Challenge? = null,
    val completedChallenge: Challenge? = null,
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
    private val preferencesRepository: com.main.sugarbreak.domain.repository.PreferencesRepository,
    private val challengeRepository: com.main.sugarbreak.domain.repository.ChallengeRepository,
    private val createChallengeUseCase: com.main.sugarbreak.domain.usecase.CreateChallengeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadHomeData()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun loadHomeData() {
        combine(
            getActiveChallengeUseCase(),
            preferencesRepository.getUserName(),
            challengeRepository.getAllChallenges()
        ) { challenge, userName, allChallenges ->
            Triple(challenge, userName, allChallenges)
        }.flatMapLatest { (challenge, userName, allChallenges) ->
            val lastCompleted = allChallenges.filter { it.status == com.main.sugarbreak.domain.model.ChallengeStatus.COMPLETED }.maxByOrNull { it.id }
            if (challenge != null) {
                combine(
                    calculateStreakUseCase(challenge.id),
                    getTodayCheckInUseCase(challenge.id)
                ) { streak, checkIn ->
                    HomeState(
                        activeChallenge = challenge,
                        completedChallenge = null,
                        streakSummary = streak,
                        todayCheckIn = checkIn,
                        userName = userName,
                        isLoading = false
                    )
                }
            } else {
                flowOf(
                    HomeState(
                        activeChallenge = null,
                        completedChallenge = lastCompleted,
                        userName = userName,
                        isLoading = false
                    )
                )
            }
        }.onEach { newState ->
            _state.value = newState
        }.launchIn(viewModelScope)
    }

    fun startNewChallenge(targetDays: Int, behavior: com.main.sugarbreak.domain.model.ChallengeBehavior? = null) {
        viewModelScope.launch {
            val selectedBehavior = behavior ?: preferencesRepository.getChallengeBehavior().firstOrNull() ?: com.main.sugarbreak.domain.model.ChallengeBehavior.CONTINUE
            if (behavior != null) {
                preferencesRepository.updateChallengeBehavior(behavior)
            }
            createChallengeUseCase(targetDays, selectedBehavior)
        }
    }
}
