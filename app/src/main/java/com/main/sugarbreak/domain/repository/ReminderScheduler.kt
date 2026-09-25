package com.main.sugarbreak.domain.repository

interface ReminderScheduler {
    fun schedule(hour: Int, minute: Int)
    fun cancel()
}
