package com.main.nosugar.di

import android.content.Context
import androidx.room.Room
import com.main.nosugar.data.local.NoSugarDatabase
import com.main.nosugar.data.local.dao.ChallengeDao
import com.main.nosugar.data.local.dao.DailyCheckInDao
import com.main.nosugar.data.preferences.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideNoSugarDatabase(@ApplicationContext context: Context): NoSugarDatabase {
        return Room.databaseBuilder(
            context,
            NoSugarDatabase::class.java,
            "nosugar_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideChallengeDao(database: NoSugarDatabase): ChallengeDao {
        return database.challengeDao
    }

    @Provides
    @Singleton
    fun provideDailyCheckInDao(database: NoSugarDatabase): DailyCheckInDao {
        return database.dailyCheckInDao
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
}
