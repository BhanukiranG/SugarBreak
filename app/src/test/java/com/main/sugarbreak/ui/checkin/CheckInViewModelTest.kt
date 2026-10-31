package com.main.sugarbreak.ui.checkin

import androidx.lifecycle.SavedStateHandle
import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ChallengeStatus
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.model.SlipReason
import com.main.sugarbreak.domain.repository.ChallengeRepository
import com.main.sugarbreak.domain.repository.CheckInRepository
import com.main.sugarbreak.domain.usecase.GetActiveChallengeUseCase
import com.main.sugarbreak.domain.usecase.RecordCheckInUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class CheckInViewModelTest {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    class FakeCheckInRepository : CheckInRepository {
        val checkIns = mutableListOf<DailyCheckIn>()

        override suspend fun insert(checkIn: DailyCheckIn) {
            val idx = checkIns.indexOfFirst { it.challengeId == checkIn.challengeId && it.date == checkIn.date }
            if (idx != -1) {
                checkIns[idx] = checkIn
            } else {
                val assigned = if (checkIn.id == 0L) checkIn.copy(id = (checkIns.size + 1).toLong()) else checkIn
                checkIns.add(assigned)
            }
        }

        override suspend fun update(checkIn: DailyCheckIn) {
            val idx = checkIns.indexOfFirst { it.id == checkIn.id || (it.challengeId == checkIn.challengeId && it.date == checkIn.date) }
            if (idx != -1) {
                checkIns[idx] = checkIn
            } else {
                checkIns.add(checkIn)
            }
        }

        override suspend fun getCheckInForDate(challengeId: Long, date: LocalDate): DailyCheckIn? {
            return checkIns.find { it.challengeId == challengeId && it.date == date }
        }

        override fun getCheckInsForChallenge(challengeId: Long): Flow<List<DailyCheckIn>> = flowOf(checkIns)

        override suspend fun getAllCheckInsForChallengeSync(challengeId: Long): List<DailyCheckIn> {
            return checkIns.filter { it.challengeId == challengeId }
        }

        override suspend fun deleteAllCheckInsForChallenge(challengeId: Long) {
            checkIns.removeAll { it.challengeId == challengeId }
        }

        override suspend fun hasCompletedCheckInForDate(date: LocalDate): Boolean {
            return checkIns.any { it.date == date && it.status != CheckInStatus.PENDING }
        }

        override fun getAllCheckIns(): Flow<List<DailyCheckIn>> = flowOf(checkIns)
    }

    class FakeChallengeRepository : ChallengeRepository {
        var challenge: Challenge? = null

        override suspend fun insert(challenge: Challenge): Long {
            this.challenge = challenge
            return 1L
        }

        override suspend fun update(challenge: Challenge) {
            this.challenge = challenge
        }

        override fun getActiveChallenge(): Flow<Challenge?> = flowOf(challenge)
        override suspend fun getChallengeById(id: Long): Challenge? = challenge
        override fun getAllChallenges(): Flow<List<Challenge>> = flowOf(listOfNotNull(challenge))
    }

    private lateinit var checkInRepo: FakeCheckInRepository
    private lateinit var challengeRepo: FakeChallengeRepository
    private lateinit var getActiveChallengeUseCase: GetActiveChallengeUseCase
    private lateinit var recordCheckInUseCase: RecordCheckInUseCase

    private val startDate: LocalDate = LocalDate.now().minusDays(5)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        checkInRepo = FakeCheckInRepository()
        challengeRepo = FakeChallengeRepository()
        getActiveChallengeUseCase = GetActiveChallengeUseCase(challengeRepo)
        recordCheckInUseCase = RecordCheckInUseCase(checkInRepo, challengeRepo)

        challengeRepo.challenge = Challenge(
            id = 1L,
            startDate = startDate,
            targetSuccessfulDays = 30,
            recoveryDays = 0,
            status = ChallengeStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            behavior = ChallengeBehavior.CONTINUE
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(dateArg: String? = null): CheckInViewModel {
        val handle = if (dateArg != null) {
            SavedStateHandle(mapOf("date" to dateArg))
        } else {
            SavedStateHandle()
        }
        return CheckInViewModel(
            savedStateHandle = handle,
            getActiveChallengeUseCase = getActiveChallengeUseCase,
            checkInRepository = checkInRepo,
            recordCheckInUseCase = recordCheckInUseCase
        )
    }

    @Test
    fun `test initial load for today defaults to today's check-in`() {
        val today = LocalDate.now()
        checkInRepo.checkIns.add(
            DailyCheckIn(
                id = 1,
                challengeId = 1L,
                date = today,
                status = CheckInStatus.SUCCESS,
                reason = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals(today, state.selectedDate)
        assertEquals(startDate, state.challengeStartDate)
        assertTrue(state.isEditing)
        assertEquals(false, state.hasHadSugar)
        assertNull(state.selectedReason)
        assertTrue(state.canNavigatePreviousDate)
        assertFalse(state.canNavigateNextDate)
    }

    @Test
    fun `test initial load with previous date argument loads past record in edit mode`() {
        val pastDate = LocalDate.now().minusDays(2)
        checkInRepo.checkIns.add(
            DailyCheckIn(
                id = 2,
                challengeId = 1L,
                date = pastDate,
                status = CheckInStatus.SLIP,
                reason = SlipReason.DESSERT,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        val viewModel = createViewModel(dateArg = pastDate.toString())
        val state = viewModel.uiState.value

        assertEquals(pastDate, state.selectedDate)
        assertTrue(state.isEditing)
        assertEquals(true, state.hasHadSugar)
        assertEquals(SlipReason.DESSERT, state.selectedReason)
        assertTrue(state.canNavigatePreviousDate)
        assertTrue(state.canNavigateNextDate)
    }

    @Test
    fun `test initial load for unlogged past date loads in new check-in mode`() {
        val pastDate = LocalDate.now().minusDays(3)

        val viewModel = createViewModel(dateArg = pastDate.toString())
        val state = viewModel.uiState.value

        assertEquals(pastDate, state.selectedDate)
        assertFalse(state.isEditing)
        assertNull(state.hasHadSugar)
        assertNull(state.selectedReason)
        assertTrue(state.canNavigatePreviousDate)
        assertTrue(state.canNavigateNextDate)
    }

    @Test
    fun `test date stepper previous and next boundaries clamped to start date and today`() {
        val viewModel = createViewModel() // starts at today
        assertEquals(LocalDate.now(), viewModel.uiState.value.selectedDate)

        // Step back day by day until startDate
        var current = LocalDate.now()
        while (current.isAfter(startDate)) {
            assertTrue(viewModel.uiState.value.canNavigatePreviousDate)
            viewModel.onPreviousDate()
            current = current.minusDays(1)
            assertEquals(current, viewModel.uiState.value.selectedDate)
        }

        // At startDate, canNavigatePreviousDate must be false
        assertEquals(startDate, viewModel.uiState.value.selectedDate)
        assertFalse(viewModel.uiState.value.canNavigatePreviousDate)

        // Attempting to step back further does nothing
        viewModel.onPreviousDate()
        assertEquals(startDate, viewModel.uiState.value.selectedDate)

        // Step forward day by day until today
        while (current.isBefore(LocalDate.now())) {
            assertTrue(viewModel.uiState.value.canNavigateNextDate)
            viewModel.onNextDate()
            current = current.plusDays(1)
            assertEquals(current, viewModel.uiState.value.selectedDate)
        }

        // At today, canNavigateNextDate must be false
        assertEquals(LocalDate.now(), viewModel.uiState.value.selectedDate)
        assertFalse(viewModel.uiState.value.canNavigateNextDate)
    }

    @Test
    fun `test submitting check-in for previous date records check-in with target date and finishes when last day`() {
        val yesterday = LocalDate.now().minusDays(1)
        // Mark all past days from startDate to day -2 as logged
        var d = startDate
        var idCounter = 1L
        while (d.isBefore(yesterday)) {
            checkInRepo.checkIns.add(
                DailyCheckIn(idCounter++, 1L, d, CheckInStatus.SUCCESS, null, LocalDateTime.now(), LocalDateTime.now())
            )
            d = d.plusDays(1)
        }

        // Only yesterday is unlogged
        val viewModel = createViewModel(dateArg = yesterday.toString())

        viewModel.onTrackSelection(false) // had sugar
        viewModel.onReasonSelection(SlipReason.SWEET)
        viewModel.submitCheckIn()

        val state = viewModel.uiState.value
        assertTrue(state.isSubmitted)
        assertTrue(state.isCatchUpCompleted)
        assertFalse(state.isSuccess)

        val recorded = checkInRepo.checkIns.find { it.date == yesterday }
        org.junit.Assert.assertNotNull(recorded)
        assertEquals(CheckInStatus.SLIP, recorded?.status)
        assertEquals(SlipReason.SWEET, recorded?.reason)
        assertEquals(yesterday, recorded?.date)
    }

    @Test
    fun `test auto-advance to next unlogged day when multiple past days are missed`() {
        // Set challenge to start 2 days ago (days -2 and -1 are unlogged)
        val twoDaysAgo = LocalDate.now().minusDays(2)
        val yesterday = LocalDate.now().minusDays(1)
        challengeRepo.challenge = challengeRepo.challenge!!.copy(startDate = twoDaysAgo)

        val viewModel = createViewModel(dateArg = twoDaysAgo.toString())
        assertEquals(twoDaysAgo, viewModel.uiState.value.selectedDate)

        // Submit Day -2 as SUCCESS
        viewModel.onTrackSelection(true)
        viewModel.submitCheckIn()

        // Should automatically advance to Day -1 (yesterday)
        assertEquals(yesterday, viewModel.uiState.value.selectedDate)
        assertFalse(viewModel.uiState.value.isSubmitted)

        // Submit Day -1 as SUCCESS
        viewModel.onTrackSelection(true)
        viewModel.submitCheckIn()

        // All past missed days now logged -> catch-up complete!
        assertTrue(viewModel.uiState.value.isSubmitted)
        assertTrue(viewModel.uiState.value.isCatchUpCompleted)
    }

    @Test
    fun `test initial load date before challenge start date is clamped to challenge start date`() {
        val beforeStart = startDate.minusDays(10)
        val viewModel = createViewModel(dateArg = beforeStart.toString())

        assertEquals(startDate, viewModel.uiState.value.selectedDate)
        assertFalse(viewModel.uiState.value.canNavigatePreviousDate)
    }

    @Test
    fun `test initial load date in future is clamped to today`() {
        val future = LocalDate.now().plusDays(5)
        val viewModel = createViewModel(dateArg = future.toString())

        assertEquals(LocalDate.now(), viewModel.uiState.value.selectedDate)
        assertFalse(viewModel.uiState.value.canNavigateNextDate)
    }
}
