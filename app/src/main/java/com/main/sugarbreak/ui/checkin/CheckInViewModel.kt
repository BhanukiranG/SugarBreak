package com.main.sugarbreak.ui.checkin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.SlipReason
import com.main.sugarbreak.domain.repository.CheckInRepository
import com.main.sugarbreak.domain.usecase.GetActiveChallengeUseCase
import com.main.sugarbreak.domain.usecase.RecordCheckInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class CheckInUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val challengeStartDate: LocalDate? = null,
    val isEditing: Boolean = false,
    val hasHadSugar: Boolean? = null,
    val selectedReason: SlipReason? = null,
    val isSubmitted: Boolean = false,
    val isSuccess: Boolean = false,
    val canNavigatePreviousDate: Boolean = false,
    val canNavigateNextDate: Boolean = false,
    val remainingCatchUpDays: Int = 0,
    val isCatchUpCompleted: Boolean = false
)

@HiltViewModel
class CheckInViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getActiveChallengeUseCase: GetActiveChallengeUseCase,
    private val checkInRepository: CheckInRepository,
    private val recordCheckInUseCase: RecordCheckInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    private var currentChallenge: Challenge? = null

    init {
        val dateArg = savedStateHandle.get<String>("date")
        val initialDate = parseDateOrNull(dateArg) ?: LocalDate.now()
        loadCheckInForDate(initialDate)
    }

    private fun parseDateOrNull(dateStr: String?): LocalDate? {
        if (dateStr.isNullOrBlank()) return null
        return runCatching { LocalDate.parse(dateStr) }.getOrNull()
    }

    fun loadCheckInForDate(date: LocalDate) {
        viewModelScope.launch {
            val challenge = currentChallenge ?: getActiveChallengeUseCase().firstOrNull()
            currentChallenge = challenge

            val today = LocalDate.now()
            val startDate = challenge?.startDate ?: today

            // Clamp target date within [challenge.startDate, today]
            val clampedDate = when {
                date.isBefore(startDate) -> startDate
                date.isAfter(today) -> today
                else -> date
            }

            val checkIn = if (challenge != null) {
                checkInRepository.getCheckInForDate(challenge.id, clampedDate)
            } else null

            val isEditing = checkIn != null && checkIn.status != CheckInStatus.PENDING
            val hasHadSugar = if (isEditing) checkIn?.status == CheckInStatus.SLIP else null
            val selectedReason = if (isEditing) checkIn?.reason else null

            val remainingUnlogged = if (challenge != null) {
                countUnloggedPastDays(challenge)
            } else 0

            _uiState.update {
                it.copy(
                    selectedDate = clampedDate,
                    challengeStartDate = challenge?.startDate,
                    isEditing = isEditing,
                    hasHadSugar = hasHadSugar,
                    selectedReason = selectedReason,
                    canNavigatePreviousDate = clampedDate.isAfter(startDate),
                    canNavigateNextDate = clampedDate.isBefore(today),
                    remainingCatchUpDays = remainingUnlogged,
                    isSubmitted = false
                )
            }
        }
    }

    private suspend fun countUnloggedPastDays(challenge: Challenge): Int {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        if (challenge.startDate.isAfter(yesterday)) return 0

        val allCheckIns = checkInRepository.getAllCheckInsForChallengeSync(challenge.id)
        val completedDates = allCheckIns
            .filter { it.status != CheckInStatus.PENDING }
            .map { it.date }
            .toSet()

        val totalDays = ChronoUnit.DAYS.between(challenge.startDate, yesterday)
        var count = 0
        for (i in 0..totalDays) {
            val d = challenge.startDate.plusDays(i)
            if (d !in completedDates) {
                count++
            }
        }
        return count
    }

    private suspend fun getRemainingUnloggedPastDates(challenge: Challenge): List<LocalDate> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        if (challenge.startDate.isAfter(yesterday)) return emptyList()

        val allCheckIns = checkInRepository.getAllCheckInsForChallengeSync(challenge.id)
        val completedDates = allCheckIns
            .filter { it.status != CheckInStatus.PENDING }
            .map { it.date }
            .toSet()

        val totalDays = ChronoUnit.DAYS.between(challenge.startDate, yesterday)
        val unlogged = mutableListOf<LocalDate>()
        for (i in 0..totalDays) {
            val d = challenge.startDate.plusDays(i)
            if (d !in completedDates) {
                unlogged.add(d)
            }
        }
        return unlogged
    }

    fun onPreviousDate() {
        val current = _uiState.value.selectedDate
        val start = _uiState.value.challengeStartDate ?: current
        if (current.isAfter(start)) {
            loadCheckInForDate(current.minusDays(1))
        }
    }

    fun onNextDate() {
        val current = _uiState.value.selectedDate
        val today = LocalDate.now()
        if (current.isBefore(today)) {
            loadCheckInForDate(current.plusDays(1))
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
        val wasLoggingUnloggedPastDay = !state.isEditing && state.selectedDate.isBefore(LocalDate.now())

        viewModelScope.launch {
            val challenge = currentChallenge ?: getActiveChallengeUseCase().firstOrNull()
            if (challenge != null) {
                recordCheckInUseCase(challenge, status, reason, date = state.selectedDate)

                if (wasLoggingUnloggedPastDay) {
                    val remainingUnlogged = getRemainingUnloggedPastDates(challenge)
                    if (remainingUnlogged.isNotEmpty()) {
                        // Auto-advance to the next unlogged day in sequence
                        loadCheckInForDate(remainingUnlogged.first())
                    } else {
                        // Finished catching up all past missed days
                        _uiState.update {
                            it.copy(
                                isSubmitted = true,
                                isSuccess = !hasHadSugar,
                                isCatchUpCompleted = true
                            )
                        }
                    }
                } else {
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
}
