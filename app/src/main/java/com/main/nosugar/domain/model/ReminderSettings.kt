package com.main.nosugar.domain.model

data class ReminderSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)
