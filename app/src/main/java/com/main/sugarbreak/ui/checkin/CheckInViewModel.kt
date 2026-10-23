package com.main.sugarbreak.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.SlipReason
import com.main.sugarbreak.domain.usecase.GetActiveChallengeUseCase
import com.main.sugarbreak.domain.usecase.GetTodayCheckInUseCase
import com.main.sugarbreak.domain.usecase.RecordCheckInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckInUiState(
    val isEditing: Boolean = false,
    val hasHadSugar: Boolean? = null,
    val selectedReason: SlipReason? = null,
    val isSubmitted: Boolean = false,
    val isSuccess: Boolean = false
)

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val getActiveChallengeUseCase: GetActiveChallengeUseCase,
    private val getTodayCheckInUseCase: GetTodayCheckInUseCase,
    private val recordCheckInUseCase: RecordCheckInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    init {
        loadTodayCheckIn()
    }

    private fun loadTodayCheckIn() {
        viewModelScope.launch {
            val challenge = getActiveChallengeUseCase().firstOrNull()
            if (challenge != null) {
                val todayCheckIn = getTodayCheckInUseCase(challenge.id).firstOrNull()
                if (todayCheckIn != null && todayCheckIn.status != CheckInStatus.PENDING) {
                    _uiState.update {
                        it.copy(
                            isEditing = true,
                            hasHadSugar = todayCheckIn.status == CheckInStatus.SLIP,
                            selectedReason = todayCheckIn.reason
                        )
                    }
                }
            }
        }
    }

    fun onTrackSelection(stayedOnTrack: Boolean) {
        _uiState.update { it.copy(hasHadSugar = !stayedOnTrack) }
    }

    fun onReasonSelection(reason: SlipReason) {
        _uiState.update { it.copy(selectedReason = reason) }
    }

    fun submitCheckIn() {
        val state = _uiState.value
        val hasHadSugar = state.hasHadSugar ?: return
        
        val status = if (hasHadSugar) CheckInStatus.SLIP else CheckInStatus.SUCCESS
        val reason = if (hasHadSugar) state.selectedReason else null

        viewModelScope.launch {
            val challenge = getActiveChallengeUseCase().firstOrNull()
            if (challenge != null) {
                recordCheckInUseCase(challenge, status, reason)
                _uiState.update { 
                    it.copy(
                        isSubmitted = true,
                        isSuccess = !hasHadSugar
                    ) 
                }
            }
        }
    }
}
