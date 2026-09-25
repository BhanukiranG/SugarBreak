package com.main.nosugar.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class DailyCheckIn(
    val id: Long,
    val challengeId: Long,
    val date: LocalDate,
    val status: CheckInStatus,
    val reason: SlipReason?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
