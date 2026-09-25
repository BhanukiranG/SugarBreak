package com.main.nosugar.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Challenge(
    val id: Long,
    val startDate: LocalDate,
    val targetSuccessfulDays: Int,
    val recoveryDays: Int,
    val status: ChallengeStatus,
    val createdAt: LocalDateTime,
    val behavior: ChallengeBehavior
)
