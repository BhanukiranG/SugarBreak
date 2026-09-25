package com.main.nosugar.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.nosugar.domain.model.Challenge
import com.main.nosugar.domain.model.DailyCheckIn
import com.main.nosugar.domain.repository.CheckInRepository
import com.main.nosugar.domain.usecase.GetActiveChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

data class HistoryUiState(
    val challenge: Challenge? = null,
    val records: List<DailyCheckIn> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getActiveChallengeUseCase: GetActiveChallengeUseCase,
    private val checkInRepository: CheckInRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getActiveChallengeUseCase().collectLatest { challenge ->
                if (challenge != null) {
                    _uiState.update { it.copy(challenge = challenge, isLoading = true) }
                    checkInRepository.getCheckInsForChallenge(challenge.id).collectLatest { records ->
                        _uiState.update { it.copy(records = records, isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(challenge = null, records = emptyList(), isLoading = false) }
                }
            }
        }
    }
}
