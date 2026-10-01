package com.main.sugarbreak.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "daily_check_ins",
    indices = [Index(value = ["challengeId", "date"], unique = true)]
)
data class DailyCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val challengeId: Long,
    val date: LocalDate,
    val status: String,
    val reason: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
