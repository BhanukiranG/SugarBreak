package com.main.sugarbreak.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var preferencesRepository: com.main.sugarbreak.domain.repository.PreferencesRepository

    @Inject
    lateinit var reminderScheduler: com.main.sugarbreak.domain.repository.ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        notificationHelper.showNotification()

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
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
