package com.main.sugarbreak.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.repository.CheckInRepository
import com.main.sugarbreak.domain.usecase.GetActiveChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
            combine(
                getActiveChallengeUseCase(),
                checkInRepository.getAllCheckIns()
            ) { challenge, records ->
                Pair(challenge, records)
            }.collectLatest { (challenge, records) ->
                _uiState.update { 
                    it.copy(
                        challenge = challenge,
                        records = records,
                        isLoading = false
                    ) 
                }
            }
        }
    }
}
