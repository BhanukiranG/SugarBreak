package com.main.sugarbreak.di

import com.main.sugarbreak.data.repository.ChallengeRepositoryImpl
import com.main.sugarbreak.data.repository.CheckInRepositoryImpl
import com.main.sugarbreak.data.repository.PreferencesRepositoryImpl
import com.main.sugarbreak.data.repository.FirebaseAuthenticationRepository
import com.main.sugarbreak.data.repository.FirestoreProgressSyncRepository
import com.main.sugarbreak.domain.repository.AuthenticationRepository
import com.main.sugarbreak.domain.repository.ProgressSyncRepository
import com.main.sugarbreak.domain.repository.ChallengeRepository
import com.main.sugarbreak.domain.repository.CheckInRepository
import com.main.sugarbreak.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import com.main.sugarbreak.BuildConfig
import com.main.sugarbreak.data.repository.RestAuthenticationRepository
import com.main.sugarbreak.data.repository.RestProgressSyncRepository
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    companion object {
        @Provides
        @Singleton
        fun authentication(firebase: javax.inject.Provider<FirebaseAuthenticationRepository>,
                           rest: javax.inject.Provider<RestAuthenticationRepository>): AuthenticationRepository =
            if (BuildConfig.SUGARBREAK_API_URL.isEmpty()) firebase.get() else rest.get()

        @Provides
        @Singleton
        fun progress(firebase: javax.inject.Provider<FirestoreProgressSyncRepository>,
                     rest: javax.inject.Provider<RestProgressSyncRepository>): ProgressSyncRepository =
            if (BuildConfig.SUGARBREAK_API_URL.isEmpty()) firebase.get() else rest.get()
    }

    @Binds
    @Singleton
    abstract fun bindChallengeRepository(
        challengeRepositoryImpl: ChallengeRepositoryImpl
    ): ChallengeRepository

    @Binds
    @Singleton
    abstract fun bindCheckInRepository(
        checkInRepositoryImpl: CheckInRepositoryImpl
    ): CheckInRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        preferencesRepositoryImpl: PreferencesRepositoryImpl
    ): PreferencesRepository
}
