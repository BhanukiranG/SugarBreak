package com.main.sugarbreak.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "challenges", indices = [Index(value = ["ownerUid", "syncId"], unique = true)])
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDate: LocalDate,
    val targetSuccessfulDays: Int,
    val recoveryDays: Int,
    val status: String,
    val createdAt: LocalDateTime,
    val behavior: String,
    @ColumnInfo(defaultValue = "''") val ownerUid: String = "",
    @ColumnInfo(defaultValue = "''") val syncId: String = "",
    @ColumnInfo(defaultValue = "''") val updatedAt: LocalDateTime = createdAt
)
