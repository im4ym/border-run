package com.borderrun.app.di

import com.borderrun.app.data.repository.CountryRepositoryImpl
import com.borderrun.app.domain.repository.CountryRepository
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
     * The binding is [@Singleton][Singleton] to ensure the offline-first
     * cache and network client are shared across all callers.
     *
     * @param impl The concrete [CountryRepositoryImpl] provided via [@Inject][javax.inject.Inject].
     * @return The [CountryRepository] interface implemented by [impl].
     */
    @Binds
    @Singleton
    abstract fun bindCountryRepository(
        impl: CountryRepositoryImpl,
    ): CountryRepository
}
