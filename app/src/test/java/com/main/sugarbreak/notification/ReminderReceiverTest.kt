package com.main.sugarbreak.notification

import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ChallengeStatus
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.model.ReminderSettings
import com.main.sugarbreak.domain.repository.ChallengeRepository
import com.main.sugarbreak.domain.repository.CheckInRepository
import com.main.sugarbreak.domain.repository.PreferencesRepository
import com.main.sugarbreak.domain.repository.ReminderScheduler
import com.main.sugarbreak.domain.usecase.GetActiveChallengeUseCase
import com.main.sugarbreak.domain.usecase.GetTodayCheckInUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ReminderReceiverTest {

    class FakeNotificationHelper : NotificationHelper() {
        var showCount = 0
        var cancelCount = 0

        override fun showNotification() {
            showCount++
        }

        override fun cancelNotification() {
            cancelCount++
        }
    }

    class FakePreferencesRepository(
        var reminderSettings: ReminderSettings = ReminderSettings(enabled = true, hour = 20, minute = 0)
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

    class FakeCheckInRepository : CheckInRepository {
        val checkIns = mutableListOf<DailyCheckIn>()

        override suspend fun insert(checkIn: DailyCheckIn) {
            checkIns.add(checkIn)
        }

        override suspend fun update(checkIn: DailyCheckIn) {}
        override suspend fun getCheckInForDate(challengeId: Long, date: LocalDate): DailyCheckIn? =
            checkIns.find { it.challengeId == challengeId && it.date == date }

        override fun getCheckInsForChallenge(challengeId: Long): Flow<List<DailyCheckIn>> = flowOf(checkIns)
        override suspend fun getAllCheckInsForChallengeSync(challengeId: Long): List<DailyCheckIn> = checkIns
        override suspend fun deleteAllCheckInsForChallenge(challengeId: Long) {}
        override suspend fun hasCompletedCheckInForDate(date: LocalDate): Boolean =
            checkIns.any { it.date == date && it.status != CheckInStatus.PENDING }

        override fun getAllCheckIns(): Flow<List<DailyCheckIn>> = flowOf(checkIns)
    }

    class FakeChallengeRepository(var activeChallenge: Challenge? = null) : ChallengeRepository {
        override suspend fun insert(challenge: Challenge): Long = 1L
        override suspend fun update(challenge: Challenge) {
            activeChallenge = challenge
        }
        override fun getActiveChallenge(): Flow<Challenge?> = flowOf(activeChallenge)
        override suspend fun getChallengeById(id: Long): Challenge? = activeChallenge
        override fun getAllChallenges(): Flow<List<Challenge>> = flowOf(listOfNotNull(activeChallenge))
    }

    private fun createActiveChallenge(): Challenge {
        return Challenge(
            id = 1L,
            startDate = LocalDate.now(),
            targetSuccessfulDays = 30,
            recoveryDays = 0,
            status = ChallengeStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            behavior = ChallengeBehavior.CONTINUE
        )
    }

    private fun createReceiver(
        notifHelper: FakeNotificationHelper,
        prefsRepo: FakePreferencesRepository,
        scheduler: FakeReminderScheduler,
        checkInRepo: FakeCheckInRepository,
        challengeRepo: FakeChallengeRepository
    ): ReminderReceiver {
        val receiver = ReminderReceiver()
        receiver.notificationHelper = notifHelper
        receiver.preferencesRepository = prefsRepo
        receiver.reminderScheduler = scheduler
        receiver.checkInRepository = checkInRepo
        receiver.getActiveChallengeUseCase = GetActiveChallengeUseCase(challengeRepo)
        receiver.getTodayCheckInUseCase = GetTodayCheckInUseCase(checkInRepo)
        return receiver
    }

    @Test
    fun `test reminder shows notification when user has not checked in and has active challenge`() = runBlocking {
        val notifHelper = FakeNotificationHelper()
        val prefsRepo = FakePreferencesRepository()
        val scheduler = FakeReminderScheduler()
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository(createActiveChallenge())

        val receiver = createReceiver(notifHelper, prefsRepo, scheduler, checkInRepo, challengeRepo)
        receiver.processReminder(LocalDate.now())

        assertEquals(1, notifHelper.showCount)
        assertEquals(0, notifHelper.cancelCount)
    }

    @Test
    fun `test reminder cancels notification when user has already checked in today with SUCCESS`() = runBlocking {
        val notifHelper = FakeNotificationHelper()
        val prefsRepo = FakePreferencesRepository()
        val scheduler = FakeReminderScheduler()
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository(createActiveChallenge())

        // Add today's completed check-in
        checkInRepo.insert(
            DailyCheckIn(
                id = 1L,
                challengeId = 1L,
                date = LocalDate.now(),
                status = CheckInStatus.SUCCESS,
                reason = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        val receiver = createReceiver(notifHelper, prefsRepo, scheduler, checkInRepo, challengeRepo)
        receiver.processReminder(LocalDate.now())

        assertEquals(0, notifHelper.showCount)
        assertEquals(1, notifHelper.cancelCount)
    }

    @Test
    fun `test reminder cancels notification when user has already checked in today with SLIP`() = runBlocking {
        val notifHelper = FakeNotificationHelper()
        val prefsRepo = FakePreferencesRepository()
        val scheduler = FakeReminderScheduler()
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository(createActiveChallenge())

        // Add today's slip check-in
        checkInRepo.insert(
            DailyCheckIn(
                id = 1L,
                challengeId = 1L,
                date = LocalDate.now(),
                status = CheckInStatus.SLIP,
                reason = com.main.sugarbreak.domain.model.SlipReason.SWEET,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        val receiver = createReceiver(notifHelper, prefsRepo, scheduler, checkInRepo, challengeRepo)
        receiver.processReminder(LocalDate.now())

        assertEquals(0, notifHelper.showCount)
        assertEquals(1, notifHelper.cancelCount)
    }

    @Test
    fun `test reminder does not show notification when user has no active challenge`() = runBlocking {
        val notifHelper = FakeNotificationHelper()
        val prefsRepo = FakePreferencesRepository()
        val scheduler = FakeReminderScheduler()
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository(null) // No active challenge

        val receiver = createReceiver(notifHelper, prefsRepo, scheduler, checkInRepo, challengeRepo)
        receiver.processReminder(LocalDate.now())

        assertEquals(0, notifHelper.showCount)
        assertEquals(0, notifHelper.cancelCount)
    }

    @Test
    fun `test reminder shows notification when check in status is PENDING`() = runBlocking {
        val notifHelper = FakeNotificationHelper()
        val prefsRepo = FakePreferencesRepository()
        val scheduler = FakeReminderScheduler()
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository(createActiveChallenge())

        // Pending check-in should not count as completed
        checkInRepo.insert(
            DailyCheckIn(
                id = 1L,
                challengeId = 1L,
                date = LocalDate.now(),
                status = CheckInStatus.PENDING,
                reason = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        val receiver = createReceiver(notifHelper, prefsRepo, scheduler, checkInRepo, challengeRepo)
        receiver.processReminder(LocalDate.now())

        assertEquals(1, notifHelper.showCount)
        assertEquals(0, notifHelper.cancelCount)
    }

    @Test
    fun `test reminder reschedules next alarm when reminder setting is enabled`() = runBlocking {
        val notifHelper = FakeNotificationHelper()
        val prefsRepo = FakePreferencesRepository(ReminderSettings(enabled = true, hour = 21, minute = 30))
        val scheduler = FakeReminderScheduler()
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository(createActiveChallenge())

        val receiver = createReceiver(notifHelper, prefsRepo, scheduler, checkInRepo, challengeRepo)
        receiver.processReminder(LocalDate.now())

        assertEquals(21, scheduler.scheduledHour)
        assertEquals(30, scheduler.scheduledMinute)
    }

    @Test
    fun `test reminder does not reschedule when reminder setting is disabled`() = runBlocking {
        val notifHelper = FakeNotificationHelper()
        val prefsRepo = FakePreferencesRepository(ReminderSettings(enabled = false, hour = 21, minute = 30))
        val scheduler = FakeReminderScheduler()
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository(createActiveChallenge())

        val receiver = createReceiver(notifHelper, prefsRepo, scheduler, checkInRepo, challengeRepo)
        receiver.processReminder(LocalDate.now())

        assertNull(scheduler.scheduledHour)
        assertNull(scheduler.scheduledMinute)
    }
}
