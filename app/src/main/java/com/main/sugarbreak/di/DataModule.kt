package com.main.sugarbreak.di

import android.content.Context
import androidx.room.Room
import com.main.sugarbreak.data.local.SugarBreakDatabase
import com.main.sugarbreak.data.local.dao.ChallengeDao
import com.main.sugarbreak.data.local.dao.DailyCheckInDao
import com.main.sugarbreak.data.preferences.PreferencesManager
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
    fun provideSugarBreakDatabase(@ApplicationContext context: Context): SugarBreakDatabase {
        return Room.databaseBuilder(
            context,
            SugarBreakDatabase::class.java,
            "sugarbreak_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideChallengeDao(database: SugarBreakDatabase): ChallengeDao {
        return database.challengeDao
    }

    @Provides
    @Singleton
    fun provideDailyCheckInDao(database: SugarBreakDatabase): DailyCheckInDao {
        return database.dailyCheckInDao
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }
}
