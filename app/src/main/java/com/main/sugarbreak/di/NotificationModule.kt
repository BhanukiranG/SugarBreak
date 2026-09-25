package com.main.sugarbreak.di

import com.main.sugarbreak.domain.repository.ReminderScheduler
import com.main.sugarbreak.notification.ReminderSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(
        reminderSchedulerImpl: ReminderSchedulerImpl
    ): ReminderScheduler
}
