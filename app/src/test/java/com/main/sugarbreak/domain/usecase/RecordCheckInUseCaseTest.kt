package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ChallengeStatus
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.repository.ChallengeRepository
import com.main.sugarbreak.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class RecordCheckInUseCaseTest {

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

    private fun createChallenge(behavior: ChallengeBehavior, target: Int = 30): Challenge {
        return Challenge(
            id = 1L,
            startDate = LocalDate.now(),
            targetSuccessfulDays = target,
            recoveryDays = 0,
            status = ChallengeStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            behavior = behavior
        )
    }

    @Test
    fun `test recovery-day behavior`() = runBlocking {
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository()
        val useCase = RecordCheckInUseCase(checkInRepo, challengeRepo)

        val challenge = createChallenge(ChallengeBehavior.ADD_RECOVERY_DAY, 30)
        challengeRepo.insert(challenge)

        useCase(challenge, CheckInStatus.SLIP, null)

        val updatedChallenge = challengeRepo.challenge!!
        assertEquals(1, updatedChallenge.recoveryDays)
        assertEquals(ChallengeStatus.ACTIVE, updatedChallenge.status)
    }

    @Test
    fun `test challenge completion`() = runBlocking {
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository()
        val useCase = RecordCheckInUseCase(checkInRepo, challengeRepo)

        val challenge = createChallenge(ChallengeBehavior.CONTINUE, 1) // Only 1 day needed
        challengeRepo.insert(challenge)

        useCase(challenge, CheckInStatus.SUCCESS, null)

        val updatedChallenge = challengeRepo.challenge!!
        assertEquals(0, updatedChallenge.recoveryDays)
        assertEquals(ChallengeStatus.COMPLETED, updatedChallenge.status)
    }

    @Test
    fun `test challenge completion with CONTINUE rule and slips`() = runBlocking {
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository()
        val useCase = RecordCheckInUseCase(checkInRepo, challengeRepo)

        val challenge = createChallenge(ChallengeBehavior.CONTINUE, 2)
        challengeRepo.insert(challenge)

        // Day 1 slip
        checkInRepo.insert(
            DailyCheckIn(
                id = 1,
                challengeId = challenge.id,
                date = LocalDate.now().minusDays(1),
                status = CheckInStatus.SLIP,
                reason = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )

        // Day 2 success (2 total tracked days = target 2)
        useCase(challenge, CheckInStatus.SUCCESS, null)

        val updatedChallenge = challengeRepo.challenge!!
        assertEquals(ChallengeStatus.COMPLETED, updatedChallenge.status)
    }

    @Test
    fun `test edit check-in from SUCCESS to SLIP increases recovery day`() = runBlocking {
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository()
        val useCase = RecordCheckInUseCase(checkInRepo, challengeRepo)

        val challenge = createChallenge(ChallengeBehavior.ADD_RECOVERY_DAY, 30)
        challengeRepo.insert(challenge)

        // Initial check-in: Success (earlier in the day)
        useCase(challenge, CheckInStatus.SUCCESS, null)
        var updatedChallenge = challengeRepo.challenge!!
        assertEquals(0, updatedChallenge.recoveryDays)

        // Later in the evening: User ate sugar, edits check-in to SLIP
        useCase(updatedChallenge, CheckInStatus.SLIP, com.main.sugarbreak.domain.model.SlipReason.DESSERT)
        updatedChallenge = challengeRepo.challenge!!
        assertEquals(1, updatedChallenge.recoveryDays)
        assertEquals(CheckInStatus.SLIP, checkInRepo.getCheckInForDate(challenge.id, LocalDate.now())?.status)
        assertEquals(com.main.sugarbreak.domain.model.SlipReason.DESSERT, checkInRepo.getCheckInForDate(challenge.id, LocalDate.now())?.reason)
    }

    @Test
    fun `test edit check-in from SLIP to SUCCESS decreases recovery day`() = runBlocking {
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository()
        val useCase = RecordCheckInUseCase(checkInRepo, challengeRepo)

        val challenge = createChallenge(ChallengeBehavior.ADD_RECOVERY_DAY, 30)
        challengeRepo.insert(challenge)

        // Initial check-in: Slip
        useCase(challenge, CheckInStatus.SLIP, com.main.sugarbreak.domain.model.SlipReason.SWEET)
        var updatedChallenge = challengeRepo.challenge!!
        assertEquals(1, updatedChallenge.recoveryDays)

        // User corrects check-in to SUCCESS
        useCase(updatedChallenge, CheckInStatus.SUCCESS, null)
        updatedChallenge = challengeRepo.challenge!!
        assertEquals(0, updatedChallenge.recoveryDays)
        assertEquals(CheckInStatus.SUCCESS, checkInRepo.getCheckInForDate(challenge.id, LocalDate.now())?.status)
    }

    @Test
    fun `test edit check-in reason only does not alter recovery days`() = runBlocking {
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository()
        val useCase = RecordCheckInUseCase(checkInRepo, challengeRepo)

        val challenge = createChallenge(ChallengeBehavior.ADD_RECOVERY_DAY, 30)
        challengeRepo.insert(challenge)

        // Initial check-in: Slip with SWEET
        useCase(challenge, CheckInStatus.SLIP, com.main.sugarbreak.domain.model.SlipReason.SWEET)
        var updatedChallenge = challengeRepo.challenge!!
        assertEquals(1, updatedChallenge.recoveryDays)

        // User updates reason to DESSERT
        useCase(updatedChallenge, CheckInStatus.SLIP, com.main.sugarbreak.domain.model.SlipReason.DESSERT)
        updatedChallenge = challengeRepo.challenge!!
        assertEquals(1, updatedChallenge.recoveryDays)
        assertEquals(com.main.sugarbreak.domain.model.SlipReason.DESSERT, checkInRepo.getCheckInForDate(challenge.id, LocalDate.now())?.reason)
    }

    @Test
    fun `test edit check-in reverts completed status if success count drops below target`() = runBlocking {
        val checkInRepo = FakeCheckInRepository()
        val challengeRepo = FakeChallengeRepository()
        val useCase = RecordCheckInUseCase(checkInRepo, challengeRepo)

        val challenge = createChallenge(ChallengeBehavior.RESET_STREAK, 1) // 1 day target
        challengeRepo.insert(challenge)

        // Check in SUCCESS completes the 1-day challenge
        useCase(challenge, CheckInStatus.SUCCESS, null)
        var updatedChallenge = challengeRepo.challenge!!
        assertEquals(ChallengeStatus.COMPLETED, updatedChallenge.status)

        // Edit check-in to SLIP at night reverts status to ACTIVE
        useCase(updatedChallenge, CheckInStatus.SLIP, com.main.sugarbreak.domain.model.SlipReason.SWEET)
        updatedChallenge = challengeRepo.challenge!!
        assertEquals(ChallengeStatus.ACTIVE, updatedChallenge.status)
    }
}
