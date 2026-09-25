package com.main.nosugar.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.nosugar.domain.model.StreakSummary
import com.main.nosugar.domain.usecase.CalculateStreakUseCase
import com.main.nosugar.domain.usecase.GetActiveChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val successfulDays: Int = 0,
    val slipDays: Int = 0,
    val successRate: Float = 0f,
    val challengeProgress: Float = 0f,
    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val getActiveChallengeUseCase: GetActiveChallengeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            getActiveChallengeUseCase().collectLatest { challenge ->
                if (challenge != null) {
                    calculateStreakUseCase(challenge.id).collectLatest { summary ->
                        val target = challenge.targetSuccessfulDays.toFloat()
                        val progress = if (target > 0) (summary.successfulDays / target).coerceAtMost(1f) else 0f
                        
                        _uiState.update { 
                            it.copy(
                                currentStreak = summary.currentStreak,
                                bestStreak = summary.bestStreak,
                                successfulDays = summary.successfulDays,
                                slipDays = summary.slipDays,
                                successRate = summary.successRate,
                                challengeProgress = progress,
                                isLoading = false
                            ) 
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}
