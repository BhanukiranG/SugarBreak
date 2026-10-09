package com.main.sugarbreak.ui.onboarding

import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ReminderSettings
import com.main.sugarbreak.domain.repository.ChallengeRepository
import com.main.sugarbreak.domain.repository.PreferencesRepository
import com.main.sugarbreak.domain.repository.ReminderScheduler
import com.main.sugarbreak.domain.usecase.CreateChallengeUseCase
import com.main.sugarbreak.domain.usecase.ScheduleReminderUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    class FakePreferencesRepository : PreferencesRepository {
        var reminderSettings = ReminderSettings(enabled = false, hour = 20, minute = 0)
        var challengeBehavior = ChallengeBehavior.CONTINUE
        var onboardingCompleted = false
        var userName = ""
        var isDarkMode: Boolean? = null
        var appTheme: com.main.sugarbreak.domain.model.AppTheme = com.main.sugarbreak.domain.model.AppTheme.MINT
        override fun getIsDarkMode(): Flow<Boolean?> = flowOf(isDarkMode)
        override suspend fun setIsDarkMode(isDark: Boolean?) {
            isDarkMode = isDark
        }
        override fun getAppTheme(): Flow<com.main.sugarbreak.domain.model.AppTheme> = flowOf(appTheme)
        override suspend fun setAppTheme(theme: com.main.sugarbreak.domain.model.AppTheme) {
            appTheme = theme
        }


        override fun getReminderSettings(): Flow<ReminderSettings> = flowOf(reminderSettings)

        override suspend fun updateReminderSettings(settings: ReminderSettings) {
            reminderSettings = settings
        }

        override fun getChallengeBehavior(): Flow<ChallengeBehavior> = flowOf(challengeBehavior)

        override suspend fun updateChallengeBehavior(behavior: ChallengeBehavior) {
            challengeBehavior = behavior
        }

        override fun getOnboardingCompleted(): Flow<Boolean> = flowOf(onboardingCompleted)

        override suspend fun setOnboardingCompleted(completed: Boolean) {
            onboardingCompleted = completed
        }

        override fun getUserName(): Flow<String> = flowOf(userName)

        override suspend fun setUserName(name: String) {
            userName = name
        }
    }

    class FakeChallengeRepository : ChallengeRepository {
        var createdChallenge: Challenge? = null
        override suspend fun insert(challenge: Challenge): Long {
            createdChallenge = challenge
            return 1L
        }
        override suspend fun update(challenge: Challenge) {}
        override fun getActiveChallenge(): Flow<Challenge?> = flowOf(null)
        override suspend fun getChallengeById(id: Long): Challenge? = null
        override fun getAllChallenges(): Flow<List<Challenge>> = flowOf(emptyList())
    }

    class FakeReminderScheduler : ReminderScheduler {
        var scheduledHour: Int? = null
        var scheduledMinute: Int? = null
        override fun schedule(hour: Int, minute: Int) {
            scheduledHour = hour
            scheduledMinute = minute
        }
        override fun cancel() {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `finishOnboarding updates reminder settings to enabled in PreferencesRepository`() {
        val prefsRepo = FakePreferencesRepository()
        val challengeRepo = FakeChallengeRepository()
        val reminderScheduler = FakeReminderScheduler()

        val createChallengeUseCase = CreateChallengeUseCase(challengeRepo)
        val scheduleReminderUseCase = ScheduleReminderUseCase(reminderScheduler)

        val viewModel = OnboardingViewModel(
            createChallengeUseCase = createChallengeUseCase,
            scheduleReminderUseCase = scheduleReminderUseCase,
            preferencesRepository = prefsRepo
        )

        viewModel.setReminderTime(21, 30)
        viewModel.finishOnboarding()

        assertTrue(prefsRepo.reminderSettings.enabled)
        assertEquals(21, prefsRepo.reminderSettings.hour)
        assertEquals(30, prefsRepo.reminderSettings.minute)
        assertTrue(prefsRepo.onboardingCompleted)
        assertEquals(21, reminderScheduler.scheduledHour)
        assertEquals(30, reminderScheduler.scheduledMinute)
    }
}
