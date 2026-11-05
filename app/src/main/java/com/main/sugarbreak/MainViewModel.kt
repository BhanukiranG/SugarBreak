package com.main.sugarbreak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.sugarbreak.domain.repository.PreferencesRepository
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import com.main.sugarbreak.domain.repository.ProgressSyncRepository
import com.main.sugarbreak.domain.usecase.resolveAppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val authenticationRepository: AuthenticationRepository,
    private val progressSyncRepository: ProgressSyncRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    val isDarkMode: StateFlow<Boolean?> = preferencesRepository.getIsDarkMode().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), null)
    val appTheme: StateFlow<com.main.sugarbreak.domain.model.AppTheme> = preferencesRepository.getAppTheme().stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), com.main.sugarbreak.domain.model.AppTheme.MINT)

    init {
        viewModelScope.launch {
            combine(authenticationRepository.currentUser, preferencesRepository.getOnboardingCompleted()) { user, onboardingCompleted ->
                user to onboardingCompleted
            }.collectLatest { (user, onboardingCompleted) ->
                _startDestination.value = resolveAppRoute(user, onboardingCompleted)
                if (user != null && user.emailVerified) {
                    runCatching { progressSyncRepository.syncNow(user.uid) }
                }
            }
        }
    }

    fun refreshAuthenticationRoute() {
        viewModelScope.launch {
            val user = authenticationRepository.currentUserSnapshot()
            val onboardingCompleted = preferencesRepository.getOnboardingCompleted().first()
            _startDestination.value = resolveAppRoute(user, onboardingCompleted)
            if (user?.emailVerified == true) runCatching { progressSyncRepository.syncNow(user.uid) }
        }
    }
}

