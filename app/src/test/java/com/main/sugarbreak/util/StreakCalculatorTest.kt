package com.main.sugarbreak.util

import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ChallengeStatus
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.DailyCheckIn
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class StreakCalculatorTest {

    private fun createChallenge(
        startDate: LocalDate = LocalDate.now(),
        target: Int = 30,
        behavior: ChallengeBehavior = ChallengeBehavior.CONTINUE
    ): Challenge {
        return Challenge(
            id = 1L,
            startDate = startDate,
            targetSuccessfulDays = target,
            recoveryDays = 0,
            status = ChallengeStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            behavior = behavior
        )
    }

    private fun createCheckIn(
        date: LocalDate,
        status: CheckInStatus
    ): DailyCheckIn {
        return DailyCheckIn(
            id = 0L,
            challengeId = 1L,
            date = date,
            status = status,
            reason = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `test no check-ins`() {
        val summary = StreakCalculator.calculate(emptyList(), LocalDate.now())
        assertEquals(0, summary.currentStreak)
        assertEquals(0, summary.bestStreak)
        assertEquals(0, summary.successfulDays)
        assertEquals(0, summary.slipDays)
    }

    @Test
    fun `test one successful day`() {
        val today = LocalDate.now()
        val checkIns = listOf(createCheckIn(today, CheckInStatus.SUCCESS))
        val summary = StreakCalculator.calculate(checkIns, today)
        assertEquals(1, summary.currentStreak)
        assertEquals(1, summary.bestStreak)
        assertEquals(1, summary.successfulDays)
    }

    @Test
    fun `test consecutive successful days`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            createCheckIn(today.minusDays(2), CheckInStatus.SUCCESS),
            createCheckIn(today.minusDays(1), CheckInStatus.SUCCESS),
            createCheckIn(today, CheckInStatus.SUCCESS)
        )
        val summary = StreakCalculator.calculate(checkIns, today)
        assertEquals(3, summary.currentStreak)
        assertEquals(3, summary.bestStreak)
        assertEquals(3, summary.successfulDays)
    }

    @Test
    fun `test success followed by slip`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            createCheckIn(today.minusDays(1), CheckInStatus.SUCCESS),
            createCheckIn(today, CheckInStatus.SLIP)
        )
        val summary = StreakCalculator.calculate(checkIns, today)
        assertEquals(0, summary.currentStreak)
        assertEquals(1, summary.bestStreak)
        assertEquals(1, summary.successfulDays)
        assertEquals(1, summary.slipDays)
    }

    @Test
    fun `test slip followed by success`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            createCheckIn(today.minusDays(1), CheckInStatus.SLIP),
            createCheckIn(today, CheckInStatus.SUCCESS)
        )
        val summary = StreakCalculator.calculate(checkIns, today)
        assertEquals(1, summary.currentStreak)
        assertEquals(1, summary.bestStreak)
        assertEquals(1, summary.successfulDays)
        assertEquals(1, summary.slipDays)
    }

    @Test
    fun `test missing day`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            createCheckIn(today.minusDays(2), CheckInStatus.SUCCESS),
            // missing yesterday
            createCheckIn(today, CheckInStatus.SUCCESS)
        )
        val summary = StreakCalculator.calculate(checkIns, today)
        // Missing a day breaks the streak
        assertEquals(1, summary.currentStreak)
        assertEquals(1, summary.bestStreak)
        assertEquals(2, summary.successfulDays)
    }

    @Test
    fun `test multiple slips`() {
        val today = LocalDate.now()
        val checkIns = listOf(
            createCheckIn(today.minusDays(2), CheckInStatus.SLIP),
            createCheckIn(today.minusDays(1), CheckInStatus.SLIP),
            createCheckIn(today, CheckInStatus.SLIP)
        )
        val summary = StreakCalculator.calculate(checkIns, today)
        assertEquals(0, summary.currentStreak)
        assertEquals(0, summary.bestStreak)
        assertEquals(0, summary.successfulDays)
        assertEquals(3, summary.slipDays)
    }
    @Test
    fun `test challenge starting today`() {
        val today = LocalDate.now()
        // No check-ins yet, but challenge starts today
        val checkIns = emptyList<DailyCheckIn>()
        val summary = StreakCalculator.calculate(checkIns, today)
        assertEquals(0, summary.currentStreak)
        assertEquals(0, summary.bestStreak)
        assertEquals(0, summary.successfulDays)
    }

    @Test
    fun `test timezone boundary cases - simulated by custom reference date`() {
        // Assume user checks in at 11:59 PM in one timezone, which is next day in another
        // To StreakCalculator, it's just a LocalDate. We verify it calculates correctly based on the passed reference date.
        val checkInDate = LocalDate.of(2023, 10, 5)
        val checkIns = listOf(
            createCheckIn(checkInDate, CheckInStatus.SUCCESS)
        )
        
        // Scenario A: User is in timezone where today is 10/5
        val summaryA = StreakCalculator.calculate(checkIns, LocalDate.of(2023, 10, 5))
        assertEquals(1, summaryA.currentStreak)
        
        // Scenario B: User is in timezone where today is 10/6, meaning they missed a day?
        // Wait, if today is 10/6, the checkin was yesterday. It should still be a 1 day streak!
        val summaryB = StreakCalculator.calculate(checkIns, LocalDate.of(2023, 10, 6))
        assertEquals(1, summaryB.currentStreak)

        // Scenario C: User is in timezone where today is 10/7 (they missed 10/6)
        val summaryC = StreakCalculator.calculate(checkIns, LocalDate.of(2023, 10, 7))
        assertEquals(0, summaryC.currentStreak) // Streak broken!
    }
}
