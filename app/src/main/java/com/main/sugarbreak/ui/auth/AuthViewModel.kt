package com.main.sugarbreak.ui.auth

import com.main.sugarbreak.data.repository.RestAuthenticationRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthMode { SIGN_IN, REGISTER, RESET_PASSWORD, VERIFY_EMAIL }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val busy: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val authenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        val signedIn = authenticationRepository.currentUserSnapshot()
        if (signedIn != null && !signedIn.emailVerified) {
            _state.update { it.copy(mode = AuthMode.VERIFY_EMAIL, email = signedIn.email.orEmpty()) }
        }
    }

    fun setEmail(value: String) = _state.update { it.copy(email = value, error = null, message = null) }
    fun setPassword(value: String) = _state.update { it.copy(password = value, error = null) }
    fun setMode(mode: AuthMode) = _state.update { it.copy(mode = mode, error = null, message = null) }

    fun submit() = runAction {
        val current = _state.value
        when (current.mode) {
            AuthMode.SIGN_IN -> {
                authenticationRepository.signInWithEmail(current.email, current.password)
                if (authenticationRepository.currentUserSnapshot()?.emailVerified == true) {
                    _state.update { it.copy(authenticated = true) }
                } else {
                    _state.update { it.copy(mode = AuthMode.VERIFY_EMAIL, message = "Verify your email to continue.") }
                }
            }
            AuthMode.REGISTER -> {
                if (authenticationRepository is RestAuthenticationRepository) {
                    require(current.password.length >= 12) { "Use at least 12 characters." }
                    authenticationRepository.registerWithEmail(current.email, current.password)
                    _state.update { it.copy(mode = AuthMode.SIGN_IN, message = "Account created. Sign in to continue.") }
                    return@runAction
                }
                require(current.password.length >= 6) { "Password must be at least 6 characters" }
                authenticationRepository.registerWithEmail(current.email, current.password)
                _state.update { it.copy(mode = AuthMode.VERIFY_EMAIL, message = "Verification email sent. Check your inbox.") }
            }
            AuthMode.RESET_PASSWORD -> {
                authenticationRepository.sendPasswordReset(current.email)
                _state.update { it.copy(message = "Password reset email sent.") }
            }
            AuthMode.VERIFY_EMAIL -> {
                if (authenticationRepository.refreshEmailVerification()) {
                    _state.update { it.copy(message = "Email verified. Signing you in…", authenticated = true) }
                } else {
                    _state.update { it.copy(error = "Your email is not verified yet. Check your inbox and try again.") }
                }
            }
        }
    }

    fun resendVerification() = runAction {
        authenticationRepository.resendVerificationEmail()
        _state.update { it.copy(message = "Verification email sent again.") }
    }

    fun useDifferentAccount() = runAction {
        authenticationRepository.signOut()
        _state.update { it.copy(mode = AuthMode.SIGN_IN, password = "", authenticated = false) }
    }

    fun signInWithGoogleToken(idToken: String) = runAction {
        authenticationRepository.signInWithGoogleIdToken(idToken)
        _state.update { it.copy(authenticated = true) }
    }

    fun showError(message: String) = _state.update { it.copy(error = message) }

    private fun runAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null, message = null) }
            runCatching { action() }
                .onFailure { failure ->
                    val unverifiedUser = authenticationRepository.currentUserSnapshot()?.takeIf { !it.emailVerified }
                    _state.update {
                        it.copy(
                            mode = if (unverifiedUser != null) AuthMode.VERIFY_EMAIL else it.mode,
                            email = unverifiedUser?.email ?: it.email,
                            error = failure.message ?: "Authentication failed"
                        )
                    }
                }
            _state.update { it.copy(busy = false) }
        }
    }
}
