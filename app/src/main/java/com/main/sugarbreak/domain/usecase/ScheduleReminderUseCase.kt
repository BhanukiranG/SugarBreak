package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.repository.ReminderScheduler
import javax.inject.Inject

class ScheduleReminderUseCase @Inject constructor(
    private val reminderScheduler: ReminderScheduler
) {
    operator fun invoke(hour: Int, minute: Int) {
        reminderScheduler.schedule(hour, minute)
    }
}
