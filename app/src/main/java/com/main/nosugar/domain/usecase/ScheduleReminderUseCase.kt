package com.main.nosugar.domain.usecase

import com.main.nosugar.domain.repository.ReminderScheduler
import javax.inject.Inject

class ScheduleReminderUseCase @Inject constructor(
    private val reminderScheduler: ReminderScheduler
) {
    operator fun invoke(hour: Int, minute: Int) {
        reminderScheduler.schedule(hour, minute)
    }
}
