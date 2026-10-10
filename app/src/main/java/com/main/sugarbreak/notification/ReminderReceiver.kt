package com.main.sugarbreak.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var preferencesRepository: com.main.sugarbreak.domain.repository.PreferencesRepository

    @Inject
    lateinit var reminderScheduler: com.main.sugarbreak.domain.repository.ReminderScheduler

    @Inject
    lateinit var getActiveChallengeUseCase: com.main.sugarbreak.domain.usecase.GetActiveChallengeUseCase

    @Inject
    lateinit var getTodayCheckInUseCase: com.main.sugarbreak.domain.usecase.GetTodayCheckInUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val challenge = getActiveChallengeUseCase().firstOrNull()
                if (challenge != null) {
                    val checkIn = getTodayCheckInUseCase(challenge.id).firstOrNull()
                    if (checkIn == null) {
                        notificationHelper.showNotification()
                    }
                }

                val settings = preferencesRepository.getReminderSettings().first()
                if (settings.enabled) {
                    reminderScheduler.schedule(settings.hour, settings.minute)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 100
    }
}
