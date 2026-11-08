package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ChallengeStatus
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.repository.ChallengeRepository
import com.main.sugarbreak.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class GetUnloggedPastDaysUseCaseTest {

    class FakeChallengeRepository : ChallengeRepository {
        var challenge: Challenge? = null
        override suspend fun insert(challenge: Challenge): Long = 1L
        override suspend fun update(challenge: Challenge) {}
        override fun getActiveChallenge(): Flow<Challenge?> = flowOf(challenge)
        override suspend fun getChallengeById(id: Long): Challenge? = challenge
        override fun getAllChallenges(): Flow<List<Challenge>> = flowOf(listOfNotNull(challenge))
    }

    class FakeCheckInRepository : CheckInRepository {
        val checkIns = mutableListOf<DailyCheckIn>()
        override suspend fun insert(checkIn: DailyCheckIn) { checkIns.add(checkIn) }
        override suspend fun update(checkIn: DailyCheckIn) {}
        override suspend fun getCheckInForDate(challengeId: Long, date: LocalDate): DailyCheckIn? = checkIns.find { it.date == date }
        override fun getCheckInsForChallenge(challengeId: Long): Flow<List<DailyCheckIn>> = flowOf(checkIns)
        override suspend fun getAllCheckInsForChallengeSync(challengeId: Long): List<DailyCheckIn> = checkIns
        override suspend fun deleteAllCheckInsForChallenge(challengeId: Long) { checkIns.clear() }
        override suspend fun hasCompletedCheckInForDate(date: LocalDate): Boolean = checkIns.any { it.date == date }
        override fun getAllCheckIns(): Flow<List<DailyCheckIn>> = flowOf(checkIns)
    }

    @Test
    fun `test when challenge is null returns empty list`() = runBlocking {
        val challengeRepo = FakeChallengeRepository()
        val checkInRepo = FakeCheckInRepository()
        val getActiveChallengeUseCase = GetActiveChallengeUseCase(challengeRepo)
        val useCase = GetUnloggedPastDaysUseCase(getActiveChallengeUseCase, checkInRepo)

        val result = useCase().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test when challenge started today returns empty list`() = runBlocking {
        val challengeRepo = FakeChallengeRepository()
        challengeRepo.challenge = Challenge(
            id = 1L,
            startDate = LocalDate.now(),
            targetSuccessfulDays = 30,
            recoveryDays = 0,
            status = ChallengeStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            behavior = ChallengeBehavior.CONTINUE
        )
        val checkInRepo = FakeCheckInRepository()
        val getActiveChallengeUseCase = GetActiveChallengeUseCase(challengeRepo)
        val useCase = GetUnloggedPastDaysUseCase(getActiveChallengeUseCase, checkInRepo)

        val result = useCase().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test when challenge started 4 days ago with no check-ins returns all 4 past dates`() = runBlocking {
        val challengeRepo = FakeChallengeRepository()
        val startDate = LocalDate.now().minusDays(4)
        challengeRepo.challenge = Challenge(
            id = 1L,
            startDate = startDate,
            targetSuccessfulDays = 30,
            recoveryDays = 0,
            status = ChallengeStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            behavior = ChallengeBehavior.CONTINUE
        )
        val checkInRepo = FakeCheckInRepository()
        val getActiveChallengeUseCase = GetActiveChallengeUseCase(challengeRepo)
        val useCase = GetUnloggedPastDaysUseCase(getActiveChallengeUseCase, checkInRepo)

        val result = useCase().first()
        assertEquals(4, result.size)
        assertEquals(startDate, result[0])
        assertEquals(LocalDate.now().minusDays(1), result[3])
    }

    @Test
    fun `reset excludes earlier missed days and counts only dates from reset onward`() = runBlocking {
        val today = LocalDate.now()
        val challengeRepo = FakeChallengeRepository()
        val challenge = Challenge(
            id = 1L,
            startDate = today.minusDays(3),
            targetSuccessfulDays = 30,
            recoveryDays = 0,
            status = ChallengeStatus.ACTIVE,
            createdAt = today.minusDays(3).atStartOfDay(),
            behavior = ChallengeBehavior.CONTINUE,
            checkInResetDate = today
        )
        val useCase = GetUnloggedPastDaysUseCase(GetActiveChallengeUseCase(challengeRepo), FakeCheckInRepository())

        challengeRepo.challenge = challenge
        assertTrue(useCase().first().isEmpty())

        challengeRepo.challenge = challenge.copy(checkInResetDate = today.minusDays(1))
        assertEquals(listOf(today.minusDays(1)), useCase().first())
    }

    @Test
    fun `test when some past days are checked in returns only unlogged days`() = runBlocking {
        val challengeRepo = FakeChallengeRepository()
        val startDate = LocalDate.now().minusDays(4)
        challengeRepo.challenge = Challenge(
            id = 1L,
            startDate = startDate,
            targetSuccessfulDays = 30,
            recoveryDays = 0,
            status = ChallengeStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            behavior = ChallengeBehavior.CONTINUE
        )
        val checkInRepo = FakeCheckInRepository()
        // Day -4 and Day -3 are logged
        checkInRepo.checkIns.add(
            DailyCheckIn(1, 1L, startDate, CheckInStatus.SUCCESS, null, LocalDateTime.now(), LocalDateTime.now())
        )
        checkInRepo.checkIns.add(
            DailyCheckIn(2, 1L, startDate.plusDays(1), CheckInStatus.SUCCESS, null, LocalDateTime.now(), LocalDateTime.now())
        )

        val getActiveChallengeUseCase = GetActiveChallengeUseCase(challengeRepo)
        val useCase = GetUnloggedPastDaysUseCase(getActiveChallengeUseCase, checkInRepo)

        val result = useCase().first()
        assertEquals(2, result.size)
        assertEquals(startDate.plusDays(2), result[0])
        assertEquals(startDate.plusDays(3), result[1])
    }
}
