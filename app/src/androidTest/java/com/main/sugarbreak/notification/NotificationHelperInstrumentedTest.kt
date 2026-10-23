package com.main.sugarbreak.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationHelperInstrumentedTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationHelper: NotificationHelper

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Grant POST_NOTIFICATIONS runtime permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                    context.packageName,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            } catch (_: Throwable) {
            }
        }

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationHelper = NotificationHelper(context)
        // Ensure clean state before each test
        notificationHelper.cancelNotification()
    }

    @After
    fun tearDown() {
        // Clean up any posted notifications
        notificationHelper.cancelNotification()
    }

    private fun waitForActiveNotification(id: Int, timeoutMs: Long = 3000): StatusBarNotification? {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            val found = notificationManager.activeNotifications.find { it.id == id }
            if (found != null) return found
            Thread.sleep(50)
        }
        return notificationManager.activeNotifications.find { it.id == id }
    }

    private fun waitForNotificationDismissal(id: Int, timeoutMs: Long = 3000): Boolean {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            val found = notificationManager.activeNotifications.find { it.id == id }
            if (found == null) return true
            Thread.sleep(50)
        }
        return notificationManager.activeNotifications.none { it.id == id }
    }

    @Test
    fun testNotificationChannelCreatedWithHighImportance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID)
            assertNotNull("Notification channel must exist", channel)
            assertEquals(NotificationHelper.CHANNEL_ID, channel.id)
            assertEquals("Sugar Break Check-in", channel.name.toString())
            assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
        }
    }

    @Test
    fun testShowNotificationPostsActiveNotificationWithCorrectDetails() {
        notificationHelper.showNotification()

        val sugarNotification = waitForActiveNotification(NotificationHelper.NOTIFICATION_ID)
        assertNotNull("Notification with ID 1001 should be active", sugarNotification)
        val extras = sugarNotification!!.notification.extras
        assertEquals("Sugar Break Check-in", extras.getString(Notification.EXTRA_TITLE))
        assertEquals("It's time to log your sugar intake for today!", extras.getString(Notification.EXTRA_TEXT))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertEquals(NotificationHelper.CHANNEL_ID, sugarNotification.notification.channelId)
        }
    }

    @Test
    fun testCancelNotificationRemovesActiveNotification() {
        // Post notification first
        notificationHelper.showNotification()
        val beforeCancel = waitForActiveNotification(NotificationHelper.NOTIFICATION_ID)
        assertNotNull("Notification must be present before cancel", beforeCancel)

        // Cancel notification
        notificationHelper.cancelNotification()
        val dismissed = waitForNotificationDismissal(NotificationHelper.NOTIFICATION_ID)
        assertTrue("Notification must be dismissed after cancelNotification()", dismissed)
    }

    @Test
    fun testReceiversAreRegisteredInManifest() {
        val packageManager = context.packageManager

        // Verify ReminderReceiver
        val reminderReceiverComponent = ComponentName(context, ReminderReceiver::class.java)
        val reminderInfo = packageManager.getReceiverInfo(reminderReceiverComponent, PackageManager.GET_META_DATA)
        assertNotNull("ReminderReceiver must be registered in manifest", reminderInfo)
        assertTrue("ReminderReceiver must be exported", reminderInfo.exported)

        // Verify BootReceiver
        val bootReceiverComponent = ComponentName(context, BootReceiver::class.java)
        val bootInfo = packageManager.getReceiverInfo(bootReceiverComponent, PackageManager.GET_META_DATA)
        assertNotNull("BootReceiver must be registered in manifest", bootInfo)
        assertTrue("BootReceiver must be exported", bootInfo.exported)
    }
}
