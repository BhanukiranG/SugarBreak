package com.main.nosugar.domain.usecase

import com.main.nosugar.domain.model.Challenge
import com.main.nosugar.domain.model.ChallengeBehavior
import com.main.nosugar.domain.model.CheckInStatus
import com.main.nosugar.domain.model.DailyCheckIn
import com.main.nosugar.domain.model.SlipReason
import com.main.nosugar.domain.repository.ChallengeRepository
import com.main.nosugar.domain.repository.CheckInRepository
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class RecordCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val challengeRepository: ChallengeRepository
) {
    suspend operator fun invoke(
        challenge: Challenge,
        status: CheckInStatus,
        reason: SlipReason? = null
    ) {
        val checkIn = DailyCheckIn(
            id = 0,
            challengeId = challenge.id,
            date = LocalDate.now(),
            status = status,
            reason = reason,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        checkInRepository.insert(checkIn)

        val checkIns = checkInRepository.getAllCheckInsForChallengeSync(challenge.id)
        val successfulDays = checkIns.count { it.status == CheckInStatus.SUCCESS }

        var updatedChallenge = challenge
        if (status == CheckInStatus.SLIP && challenge.behavior == ChallengeBehavior.ADD_RECOVERY_DAY) {
            updatedChallenge = updatedChallenge.copy(recoveryDays = updatedChallenge.recoveryDays + 1)
        }
        
        if (successfulDays >= challenge.targetSuccessfulDays) {
            updatedChallenge = updatedChallenge.copy(status = com.main.nosugar.domain.model.ChallengeStatus.COMPLETED)
        }

        if (updatedChallenge != challenge) {
            challengeRepository.update(updatedChallenge)
        }
    }
}
