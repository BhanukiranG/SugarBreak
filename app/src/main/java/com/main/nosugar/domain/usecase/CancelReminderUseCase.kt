package com.main.nosugar.domain.usecase

import com.main.nosugar.domain.repository.ReminderScheduler
import javax.inject.Inject

class CancelReminderUseCase @Inject constructor(
    private val reminderScheduler: ReminderScheduler
) {
    operator fun invoke() {
        reminderScheduler.cancel()
    }
}
