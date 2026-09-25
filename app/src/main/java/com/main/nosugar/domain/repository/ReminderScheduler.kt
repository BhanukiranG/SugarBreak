package com.main.nosugar.domain.repository

interface ReminderScheduler {
    fun schedule(hour: Int, minute: Int)
    fun cancel()
}
