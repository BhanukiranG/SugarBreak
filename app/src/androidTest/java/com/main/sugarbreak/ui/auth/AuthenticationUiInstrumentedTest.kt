package com.main.sugarbreak.ui.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import com.main.sugarbreak.domain.model.AuthenticatedUser
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import com.main.sugarbreak.ui.onboarding.Step2Profile
import com.main.sugarbreak.ui.theme.SugarBreakTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class AuthenticationUiInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private class FakeAuthenticationRepository : AuthenticationRepository {
        override val currentUser: Flow<AuthenticatedUser?> = MutableStateFlow(null)
        override fun currentUserSnapshot(): AuthenticatedUser? = null
        override suspend fun registerWithEmail(email: String, password: String) = Unit
        override suspend fun signInWithEmail(email: String, password: String) = Unit
        override suspend fun signInWithGoogleIdToken(idToken: String) = Unit
        override suspend fun sendPasswordReset(email: String) = Unit
        override suspend fun resendVerificationEmail() = Unit
        override suspend fun refreshEmailVerification(): Boolean = false
        override suspend fun signOut() = Unit
    }

    @Test
    fun onboardingNameStepDoesNotClaimProgressIsStoredOnlyLocally() {
        composeRule.setContent {
            SugarBreakTheme {
                Step2Profile(name = "", onNameChange = {})
            }
        }

        composeRule.onAllNodesWithText("Data stored locally.").assertCountEquals(0)
        composeRule.onAllNodesWithText("Your progress is saved to your account and stays available offline.")
            .assertCountEquals(1)
    }

    @Test
    fun authenticationModesKeepTheirActionsAndRenderInAppTheme() {
        val viewModel = AuthViewModel(FakeAuthenticationRepository())
        composeRule.setContent {
            SugarBreakTheme {
                AuthScreen(viewModel = viewModel)
            }
        }

        composeRule.onAllNodesWithText("Sign in").assertCountEquals(1)
        composeRule.onAllNodesWithText("Continue with Google").assertCountEquals(1)

        composeRule.runOnIdle { viewModel.setMode(AuthMode.REGISTER) }
        composeRule.onAllNodesWithText("Create account").assertCountEquals(1)
        composeRule.onAllNodesWithText("Continue with Google").assertCountEquals(1)

        composeRule.runOnIdle { viewModel.setMode(AuthMode.RESET_PASSWORD) }
        composeRule.onAllNodesWithText("Send reset link").assertCountEquals(1)
        composeRule.onAllNodesWithText("Continue with Google").assertCountEquals(0)

        composeRule.runOnIdle { viewModel.setMode(AuthMode.VERIFY_EMAIL) }
        composeRule.onAllNodesWithText("I’ve verified my email").assertCountEquals(1)
        composeRule.onAllNodesWithText("Resend verification email").assertCountEquals(1)
    }
}
