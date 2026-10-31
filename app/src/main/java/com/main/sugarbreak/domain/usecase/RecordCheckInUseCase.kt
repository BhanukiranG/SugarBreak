package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.domain.model.SlipReason
import com.main.sugarbreak.domain.repository.ChallengeRepository
import com.main.sugarbreak.domain.repository.CheckInRepository
import com.main.sugarbreak.notification.NotificationHelper
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class RecordCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val challengeRepository: ChallengeRepository,
    private val notificationHelper: NotificationHelper? = null
) {
    suspend operator fun invoke(
        challenge: Challenge,
        status: CheckInStatus,
        reason: SlipReason? = null,
        date: LocalDate = LocalDate.now()
    ) {
        require(!date.isBefore(challenge.startDate)) {
            "Cannot record check-in before challenge start date (${challenge.startDate})"
        }
        require(!date.isAfter(LocalDate.now())) {
            "Cannot record check-in for future date ($date)"
        }

        val existing = checkInRepository.getCheckInForDate(challenge.id, date)
        val checkIn = DailyCheckIn(
            id = existing?.id ?: 0,
            challengeId = challenge.id,
            date = date,
            status = status,
            reason = reason,
            createdAt = existing?.createdAt ?: LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        if (existing != null) {
            checkInRepository.update(checkIn)
        } else {
            checkInRepository.insert(checkIn)
        }

        if (date == LocalDate.now()) {
            notificationHelper?.cancelNotification()
        }

        val checkIns = checkInRepository.getAllCheckInsForChallengeSync(challenge.id)
        val successfulDays = checkIns.count { it.status == CheckInStatus.SUCCESS }
        val slipDays = checkIns.count { it.status == CheckInStatus.SLIP }
        val totalTrackedDays = successfulDays + slipDays

        val wasSlip = existing?.status == CheckInStatus.SLIP
        val isSlip = status == CheckInStatus.SLIP
        val recoveryDiff = when {
            !wasSlip && isSlip -> 1
            wasSlip && !isSlip -> -1
            else -> 0
        }

        var updatedChallenge = challenge
        if (challenge.behavior == ChallengeBehavior.ADD_RECOVERY_DAY && recoveryDiff != 0) {
            updatedChallenge = updatedChallenge.copy(
                recoveryDays = maxOf(0, updatedChallenge.recoveryDays + recoveryDiff)
            )
        }
        
        val isCompleted = when (challenge.behavior) {
            ChallengeBehavior.CONTINUE -> totalTrackedDays >= challenge.targetSuccessfulDays
            ChallengeBehavior.ADD_RECOVERY_DAY -> successfulDays >= challenge.targetSuccessfulDays
            ChallengeBehavior.RESET_STREAK -> successfulDays >= challenge.targetSuccessfulDays
        }

        val newChallengeStatus = if (isCompleted) {
            com.main.sugarbreak.domain.model.ChallengeStatus.COMPLETED
        } else {
            com.main.sugarbreak.domain.model.ChallengeStatus.ACTIVE
        }
        updatedChallenge = updatedChallenge.copy(status = newChallengeStatus)

        if (updatedChallenge != challenge) {
            challengeRepository.update(updatedChallenge)
        }
    }
}
