package com.main.sugarbreak.domain.usecase

import com.main.sugarbreak.domain.repository.ReminderScheduler
import javax.inject.Inject

class CancelReminderUseCase @Inject constructor(
    private val reminderScheduler: ReminderScheduler
) {
    operator fun invoke() {
        reminderScheduler.cancel()
    }
}
