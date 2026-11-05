package com.main.sugarbreak.ui.auth

import com.main.sugarbreak.domain.model.AuthenticatedUser
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import com.main.sugarbreak.domain.usecase.SignOutUseCase
import com.main.sugarbreak.domain.usecase.resolveAppRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthFlowTest {
    private val dispatcher = UnconfinedTestDispatcher()

    private class FakeAuthenticationRepository : AuthenticationRepository {
        private val userFlow = MutableStateFlow<AuthenticatedUser?>(null)
        var registered = false
        var resetEmail: String? = null
        var resentVerification = false
        var signedOut = false
        var verifiedOnRefresh = false
        override val currentUser: Flow<AuthenticatedUser?> = userFlow
        override fun currentUserSnapshot() = userFlow.value
        override suspend fun registerWithEmail(email: String, password: String) {
            registered = true
            userFlow.value = AuthenticatedUser("uid", email, null, false)
        }
        override suspend fun signInWithEmail(email: String, password: String) {
            userFlow.value = AuthenticatedUser("uid", email, null, verifiedOnRefresh)
        }
        override suspend fun signInWithGoogleIdToken(idToken: String) {
            userFlow.value = AuthenticatedUser("uid", "g@example.test", "Friend", true)
        }
        override suspend fun sendPasswordReset(email: String) { resetEmail = email }
        override suspend fun resendVerificationEmail() { resentVerification = true }
        override suspend fun refreshEmailVerification(): Boolean {
            if (verifiedOnRefresh) userFlow.value = userFlow.value?.copy(emailVerified = true)
            return verifiedOnRefresh
        }
        override suspend fun signOut() { signedOut = true; userFlow.value = null }
    }

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `signed out and unverified users route to authentication`() {
        assertEquals("auth", resolveAppRoute(null, true))
        assertEquals("auth", resolveAppRoute(AuthenticatedUser("u", "e", null, false), false))
    }

    @Test fun `verified users route to onboarding or home`() {
        val user = AuthenticatedUser("u", "e", null, true)
        assertEquals("onboarding", resolveAppRoute(user, false))
        assertEquals("home", resolveAppRoute(user, true))
    }

    @Test fun `registration sends verification email and gates entry`() = runTest {
        val repository = FakeAuthenticationRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.setEmail("user@example.test")
        viewModel.setPassword("secret123")
        viewModel.setMode(AuthMode.REGISTER)
        viewModel.submit()
        assertTrue(repository.registered)
        assertEquals(AuthMode.VERIFY_EMAIL, viewModel.state.value.mode)
        assertFalse(viewModel.state.value.authenticated)
    }

    @Test fun `email login waits for verification and refresh releases the gate`() = runTest {
        val repository = FakeAuthenticationRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.setEmail("user@example.test")
        viewModel.setPassword("secret123")
        viewModel.submit()
        assertEquals(AuthMode.VERIFY_EMAIL, viewModel.state.value.mode)
        assertFalse(viewModel.state.value.authenticated)

        repository.verifiedOnRefresh = true
        viewModel.submit()
        assertTrue(viewModel.state.value.authenticated)
    }

    @Test fun `password reset sends email and reports success`() = runTest {
        val repository = FakeAuthenticationRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.setEmail("user@example.test")
        viewModel.setMode(AuthMode.RESET_PASSWORD)
        viewModel.submit()
        assertEquals("user@example.test", repository.resetEmail)
        assertTrue(viewModel.state.value.message!!.contains("sent"))
    }

    @Test fun `sign out clears the authentication session`() = runTest {
        val repository = FakeAuthenticationRepository()
        repository.signInWithGoogleIdToken("token")
        SignOutUseCase(repository)()
        assertTrue(repository.signedOut)
        assertEquals(null, repository.currentUserSnapshot())
    }
}
