package com.main.sugarbreak.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.main.sugarbreak.domain.repository.PreferencesRepository
import com.main.sugarbreak.domain.repository.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = preferencesRepository.getReminderSettings().first()
                    if (settings.enabled) {
                        reminderScheduler.schedule(settings.hour, settings.minute)
                    }
                } catch (e: Exception) {
                    // Ignore or log exceptions
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
