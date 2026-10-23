package com.main.sugarbreak.notification

import android.content.Intent
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ReminderSettings
import com.main.sugarbreak.domain.repository.PreferencesRepository
import com.main.sugarbreak.domain.repository.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BootReceiverTest {

    class FakePreferencesRepository(
        var reminderSettings: ReminderSettings = ReminderSettings(enabled = true, hour = 19, minute = 45)
    ) : PreferencesRepository {
        var challengeBehavior = ChallengeBehavior.CONTINUE
        var onboardingCompleted = true
        var userName = "Tester"
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

    class FakeReminderScheduler : ReminderScheduler {
        var scheduledHour: Int? = null
        var scheduledMinute: Int? = null
        var cancelCalled = false

        override fun schedule(hour: Int, minute: Int) {
            scheduledHour = hour
            scheduledMinute = minute
        }

        override fun cancel() {
            cancelCalled = true
        }
    }

    private fun createBootReceiver(
        prefsRepo: FakePreferencesRepository,
        scheduler: FakeReminderScheduler
    ): BootReceiver {
        val receiver = BootReceiver()
        receiver.preferencesRepository = prefsRepo
        receiver.reminderScheduler = scheduler
        return receiver
    }

    @Test
    fun `test boot completed schedules alarm when reminders enabled`() = runBlocking {
        val prefsRepo = FakePreferencesRepository(ReminderSettings(enabled = true, hour = 20, minute = 30))
        val scheduler = FakeReminderScheduler()
        val receiver = createBootReceiver(prefsRepo, scheduler)

        receiver.processIntentAction(Intent.ACTION_BOOT_COMPLETED)

        assertEquals(20, scheduler.scheduledHour)
        assertEquals(30, scheduler.scheduledMinute)
    }

    @Test
    fun `test timezone changed schedules alarm when reminders enabled`() = runBlocking {
        val prefsRepo = FakePreferencesRepository(ReminderSettings(enabled = true, hour = 8, minute = 15))
        val scheduler = FakeReminderScheduler()
        val receiver = createBootReceiver(prefsRepo, scheduler)

        receiver.processIntentAction(Intent.ACTION_TIMEZONE_CHANGED)

        assertEquals(8, scheduler.scheduledHour)
        assertEquals(15, scheduler.scheduledMinute)
    }

    @Test
    fun `test boot completed does not schedule when reminders disabled`() = runBlocking {
        val prefsRepo = FakePreferencesRepository(ReminderSettings(enabled = false, hour = 20, minute = 30))
        val scheduler = FakeReminderScheduler()
        val receiver = createBootReceiver(prefsRepo, scheduler)

        receiver.processIntentAction(Intent.ACTION_BOOT_COMPLETED)

        assertNull(scheduler.scheduledHour)
        assertNull(scheduler.scheduledMinute)
    }

    @Test
    fun `test unrelated intent action is ignored`() = runBlocking {
        val prefsRepo = FakePreferencesRepository(ReminderSettings(enabled = true, hour = 20, minute = 30))
        val scheduler = FakeReminderScheduler()
        val receiver = createBootReceiver(prefsRepo, scheduler)

        receiver.processIntentAction("android.intent.action.POWER_CONNECTED")
        assertNull(scheduler.scheduledHour)

        receiver.processIntentAction(null)
        assertNull(scheduler.scheduledHour)
    }
}
