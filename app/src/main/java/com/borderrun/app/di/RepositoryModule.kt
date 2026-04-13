package com.borderrun.app.di

import com.borderrun.app.data.repository.CountryRepositoryImpl
import com.borderrun.app.data.repository.DailyChallengeRepositoryImpl
import com.borderrun.app.data.repository.StatsRepositoryImpl
import com.borderrun.app.domain.repository.CountryRepository
import com.borderrun.app.domain.repository.DailyChallengeRepository
import com.borderrun.app.domain.repository.StatsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository interfaces (domain layer) to their
 * concrete implementations (data layer).
 *
 * Using [@Binds][Binds] instead of [@Provides][dagger.Provides] avoids
 * creating a wrapper function body, which is more efficient at compile time.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds [CountryRepositoryImpl] as the implementation of [CountryRepository].
     *
     * @param impl The concrete implementation provided via constructor injection.
     * @return The [CountryRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindCountryRepository(
        impl: CountryRepositoryImpl,
    ): CountryRepository

    /**
     * Binds [StatsRepositoryImpl] as the implementation of [StatsRepository].
     *
     * @param impl The concrete implementation provided via constructor injection.
     * @return The [StatsRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindStatsRepository(
        impl: StatsRepositoryImpl,
    ): StatsRepository

    /**
     * Binds [DailyChallengeRepositoryImpl] as the implementation of
     * [DailyChallengeRepository].
     *
     * @param impl The concrete implementation provided via constructor injection.
     * @return The [DailyChallengeRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindDailyChallengeRepository(
        impl: DailyChallengeRepositoryImpl,
    ): DailyChallengeRepository
}
