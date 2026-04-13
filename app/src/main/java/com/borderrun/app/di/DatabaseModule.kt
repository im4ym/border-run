package com.borderrun.app.di

import android.content.Context
import androidx.room.Room
import com.borderrun.app.data.local.BorderRunDatabase
import com.borderrun.app.data.local.DATABASE_NAME
import com.borderrun.app.data.local.dao.CountryDao
import com.borderrun.app.data.local.dao.DailyChallengeDao
import com.borderrun.app.data.local.dao.DailyMysteryDao
import com.borderrun.app.data.local.dao.QuizAnswerDao
import com.borderrun.app.data.local.dao.QuizSessionDao
import com.borderrun.app.data.local.dao.UserPreferencesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the Room [BorderRunDatabase] and all its DAOs.
 *
 * All bindings are [@Singleton][Singleton] so only one database instance
 * exists for the lifetime of the application process.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the singleton [BorderRunDatabase] instance.
     *
     * The database file is named [DATABASE_NAME] and is stored in the app's
     * private `databases/` directory.
     *
     * @param context Application context provided by Hilt.
     * @return The Room database instance.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BorderRunDatabase =
        Room.databaseBuilder(
            context,
            BorderRunDatabase::class.java,
            DATABASE_NAME,
        ).build()

    /**
     * Provides the [CountryDao] from the database.
     *
     * @param database The [BorderRunDatabase] singleton.
     */
    @Provides
    fun provideCountryDao(database: BorderRunDatabase): CountryDao =
        database.countryDao()

    /**
     * Provides the [QuizSessionDao] from the database.
     *
     * @param database The [BorderRunDatabase] singleton.
     */
    @Provides
    fun provideQuizSessionDao(database: BorderRunDatabase): QuizSessionDao =
        database.quizSessionDao()

    /**
     * Provides the [QuizAnswerDao] from the database.
     *
     * @param database The [BorderRunDatabase] singleton.
     */
    @Provides
    fun provideQuizAnswerDao(database: BorderRunDatabase): QuizAnswerDao =
        database.quizAnswerDao()

    /**
     * Provides the [UserPreferencesDao] from the database.
     *
     * @param database The [BorderRunDatabase] singleton.
     */
    @Provides
    fun provideUserPreferencesDao(database: BorderRunDatabase): UserPreferencesDao =
        database.userPreferencesDao()

    /**
     * Provides the [DailyChallengeDao] from the database.
     *
     * @param database The [BorderRunDatabase] singleton.
     */
    @Provides
    fun provideDailyChallengeDao(database: BorderRunDatabase): DailyChallengeDao =
        database.dailyChallengeDao()

    /**
     * Provides the [DailyMysteryDao] from the database.
     *
     * @param database The [BorderRunDatabase] singleton.
     */
    @Provides
    fun provideDailyMysteryDao(database: BorderRunDatabase): DailyMysteryDao =
        database.dailyMysteryDao()
}
