package com.main.nosugar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDate: LocalDate,
    val targetSuccessfulDays: Int,
    val recoveryDays: Int,
    val status: String,
    val createdAt: LocalDateTime,
    val behavior: String
)
