package com.main.sugarbreak

import android.app.Application
import com.main.sugarbreak.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SugarBreakApplication : Application() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        // Initialize NotificationHelper to ensure the channel is created
        notificationHelper.javaClass
    }
}
