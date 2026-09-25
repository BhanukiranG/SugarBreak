package com.main.nosugar

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import com.main.nosugar.notification.NotificationHelper
import javax.inject.Inject

@HiltAndroidApp
class NoSugarApplication : Application() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        // Initialize NotificationHelper to ensure the channel is created
        notificationHelper.javaClass
    }
}
