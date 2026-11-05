package com.main.sugarbreak.ui.settings

import com.main.sugarbreak.domain.model.AuthenticatedUser
import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.model.ReminderSettings
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import com.main.sugarbreak.domain.repository.ChallengeRepository
import com.main.sugarbreak.domain.repository.CheckInRepository
import com.main.sugarbreak.domain.repository.PreferencesRepository
import com.main.sugarbreak.domain.repository.ReminderScheduler
import com.main.sugarbreak.domain.usecase.CancelReminderUseCase
import com.main.sugarbreak.domain.usecase.CreateChallengeUseCase
import com.main.sugarbreak.domain.usecase.GetActiveChallengeUseCase
import com.main.sugarbreak.domain.usecase.ScheduleReminderUseCase
import com.main.sugarbreak.domain.usecase.SignOutUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private class FakeAuthenticationRepository : AuthenticationRepository {
        private val user = MutableStateFlow<AuthenticatedUser?>(AuthenticatedUser("user-a", "a@example.test", null, true))
        var signOutGate: CompletableDeferred<Unit>? = null
        var signOutFailure: Throwable? = null
        var signOutCalls = 0
        override val currentUser: Flow<AuthenticatedUser?> = user
        override fun currentUserSnapshot() = user.value
        override suspend fun registerWithEmail(email: String, password: String) = Unit
        override suspend fun signInWithEmail(email: String, password: String) = Unit
        override suspend fun signInWithGoogleIdToken(idToken: String) = Unit
        override suspend fun sendPasswordReset(email: String) = Unit
        override suspend fun resendVerificationEmail() = Unit
        override suspend fun refreshEmailVerification() = true
        override suspend fun signOut() {
            signOutCalls++
            signOutGate?.await()
            signOutFailure?.let { throw it }
            user.value = null
        }
    }

    private class FakePreferencesRepository : PreferencesRepository {
        override fun getReminderSettings() = flowOf(ReminderSettings(false, 20, 0))
        override suspend fun updateReminderSettings(settings: ReminderSettings) = Unit
        override fun getChallengeBehavior() = flowOf(ChallengeBehavior.CONTINUE)
        override suspend fun updateChallengeBehavior(behavior: ChallengeBehavior) = Unit
        override fun getOnboardingCompleted() = flowOf(false)
        override suspend fun setOnboardingCompleted(completed: Boolean) = Unit
        override fun getUserName() = flowOf("Friend")
        override suspend fun setUserName(name: String) = Unit
        override fun getIsDarkMode() = flowOf(null)
        override suspend fun setIsDarkMode(isDark: Boolean?) = Unit
        override fun getAppTheme() = flowOf(com.main.sugarbreak.domain.model.AppTheme.MINT)
        override suspend fun setAppTheme(theme: com.main.sugarbreak.domain.model.AppTheme) = Unit
    }

    private class FakeChallengeRepository : ChallengeRepository {
        override suspend fun insert(challenge: Challenge) = 1L
        override suspend fun update(challenge: Challenge) = Unit
        override fun getActiveChallenge() = flowOf<Challenge?>(null)
        override suspend fun getChallengeById(id: Long) = null
        override fun getAllChallenges() = flowOf(emptyList<Challenge>())
    }

    private class FakeCheckInRepository : CheckInRepository {
        override suspend fun insert(checkIn: DailyCheckIn) = Unit
        override suspend fun update(checkIn: DailyCheckIn) = Unit
        override suspend fun getCheckInForDate(challengeId: Long, date: LocalDate) = null
        override fun getCheckInsForChallenge(challengeId: Long) = flowOf(emptyList<DailyCheckIn>())
        override suspend fun getAllCheckInsForChallengeSync(challengeId: Long) = emptyList<DailyCheckIn>()
        override suspend fun deleteAllCheckInsForChallenge(challengeId: Long) = Unit
        override suspend fun hasCompletedCheckInForDate(date: LocalDate) = false
        override fun getAllCheckIns() = flowOf(emptyList<DailyCheckIn>())
    }

    private class FakeReminderScheduler : ReminderScheduler {
        override fun schedule(hour: Int, minute: Int) = Unit
        override fun cancel() = Unit
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    private fun createViewModel(auth: FakeAuthenticationRepository): SettingsViewModel {
        val challenges = FakeChallengeRepository()
        val scheduler = FakeReminderScheduler()
        return SettingsViewModel(
            preferencesRepository = FakePreferencesRepository(),
            getActiveChallengeUseCase = GetActiveChallengeUseCase(challenges),
            challengeRepository = challenges,
            checkInRepository = FakeCheckInRepository(),
            scheduleReminderUseCase = ScheduleReminderUseCase(scheduler),
            cancelReminderUseCase = CancelReminderUseCase(scheduler),
            createChallengeUseCase = CreateChallengeUseCase(challenges),
            signOutUseCase = SignOutUseCase(auth)
        )
    }

    @Test fun `sign out shows loading state and ignores duplicate taps until complete`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val auth = FakeAuthenticationRepository().apply { signOutGate = CompletableDeferred() }
        val viewModel = createViewModel(auth)

        viewModel.signOut()
        assertTrue(viewModel.uiState.value.isSigningOut)
        viewModel.signOut()
        assertEquals(1, auth.signOutCalls)

        auth.signOutGate!!.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSigningOut)
        assertEquals(null, viewModel.uiState.value.signOutError)
    }

    @Test fun `sign out failure clears loading state and exposes error`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val auth = FakeAuthenticationRepository().apply {
            signOutFailure = IllegalStateException("Sign out failed")
        }
        val viewModel = createViewModel(auth)

        viewModel.signOut()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSigningOut)
        assertEquals("Sign out failed", viewModel.uiState.value.signOutError)
    }
}
