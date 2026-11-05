package com.main.sugarbreak.data.local.entity

import androidx.room.Entity
import java.time.LocalDateTime

/** Durable request to remove a challenge's cloud check-ins after an offline reset. */
@Entity(tableName = "pending_checkin_resets", primaryKeys = ["ownerUid", "challengeSyncId"])
data class PendingCheckInResetEntity(
    val ownerUid: String,
    val challengeSyncId: String,
    val requestedAt: LocalDateTime
)
