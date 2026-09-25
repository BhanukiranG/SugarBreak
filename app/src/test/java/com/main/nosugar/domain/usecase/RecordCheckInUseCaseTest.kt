package com.main.nosugar.domain.usecase

import com.main.nosugar.domain.model.Challenge
import com.main.nosugar.domain.model.ChallengeBehavior
import com.main.nosugar.domain.model.ChallengeStatus
import com.main.nosugar.domain.model.CheckInStatus
import com.main.nosugar.domain.model.DailyCheckIn
import com.main.nosugar.domain.repository.ChallengeRepository
import com.main.nosugar.domain.repository.CheckInRepository
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
            checkIns.add(checkIn)
        }

        override suspend fun update(checkIn: DailyCheckIn) {}

        override suspend fun getCheckInForDate(challengeId: Long, date: LocalDate): DailyCheckIn? = null

        override fun getCheckInsForChallenge(challengeId: Long): Flow<List<DailyCheckIn>> = flowOf(checkIns)

        override suspend fun getAllCheckInsForChallengeSync(challengeId: Long): List<DailyCheckIn> {
            return checkIns.filter { it.challengeId == challengeId }
        }
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
}
