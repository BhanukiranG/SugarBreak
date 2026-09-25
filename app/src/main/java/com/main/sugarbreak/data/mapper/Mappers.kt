package com.main.sugarbreak.data.mapper

import com.main.sugarbreak.data.local.entity.ChallengeEntity
import com.main.sugarbreak.data.local.entity.DailyCheckInEntity
import com.main.sugarbreak.domain.model.Challenge
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.domain.model.ChallengeStatus
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.DailyCheckIn

fun ChallengeEntity.toDomain(): Challenge {
    return Challenge(
        id = id,
        startDate = startDate,
        targetSuccessfulDays = targetSuccessfulDays,
        recoveryDays = recoveryDays,
        status = ChallengeStatus.valueOf(status),
        createdAt = createdAt,
        behavior = ChallengeBehavior.valueOf(behavior)
    )
}

fun Challenge.toEntity(): ChallengeEntity {
    return ChallengeEntity(
        id = id ?: 0,
        startDate = startDate,
        targetSuccessfulDays = targetSuccessfulDays,
        recoveryDays = recoveryDays,
        status = status.name,
        createdAt = createdAt,
        behavior = behavior.name
    )
}

fun DailyCheckInEntity.toDomain(): DailyCheckIn {
    return DailyCheckIn(
        id = id,
        challengeId = challengeId,
        date = date,
        status = CheckInStatus.valueOf(status),
        reason = reason?.let { com.main.sugarbreak.domain.model.SlipReason.valueOf(it) },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun DailyCheckIn.toEntity(): DailyCheckInEntity {
    return DailyCheckInEntity(
        id = id ?: 0,
        challengeId = challengeId,
        date = date,
        status = status.name,
        reason = reason?.name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
