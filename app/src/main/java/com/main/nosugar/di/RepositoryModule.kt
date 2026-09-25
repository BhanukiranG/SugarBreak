package com.main.nosugar.di

import com.main.nosugar.data.repository.ChallengeRepositoryImpl
import com.main.nosugar.data.repository.CheckInRepositoryImpl
import com.main.nosugar.data.repository.PreferencesRepositoryImpl
import com.main.nosugar.domain.repository.ChallengeRepository
import com.main.nosugar.domain.repository.CheckInRepository
import com.main.nosugar.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

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
