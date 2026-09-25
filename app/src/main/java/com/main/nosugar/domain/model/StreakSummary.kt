package com.main.nosugar.domain.model

data class StreakSummary(
    val currentStreak: Int,
    val bestStreak: Int,
    val successfulDays: Int,
    val slipDays: Int,
    val successRate: Float
)
