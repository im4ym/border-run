package com.borderrun.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.borderrun.app.data.local.dao.CountryDao
import com.borderrun.app.data.local.dao.DailyChallengeDao
import com.borderrun.app.data.local.dao.DailyMysteryDao
import com.borderrun.app.data.local.dao.QuizAnswerDao
import com.borderrun.app.data.local.dao.QuizSessionDao
import com.borderrun.app.data.local.dao.UserPreferencesDao
import com.borderrun.app.data.local.entity.CountryEntity
import com.borderrun.app.data.local.entity.DailyChallengeEntity
import com.borderrun.app.data.local.entity.DailyMysteryEntity
import com.borderrun.app.data.local.entity.QuizAnswerEntity
import com.borderrun.app.data.local.entity.QuizSessionEntity
import com.borderrun.app.data.local.entity.UserPreferencesEntity

/** Current schema version. Increment whenever entities change. */
private const val DATABASE_VERSION = 1

/** Filename used when opening the Room database on-device. */
const val DATABASE_NAME = "borderrun.db"

/**
 * Room database for Border Run.
 *
 * Manages six tables:
 * - `countries` — cached from the RestCountries API
 * - `quiz_sessions` — completed quiz run metadata
 * - `quiz_answers` — individual answers with accuracy stats
 * - `user_preferences` — single-row user settings
 * - `daily_challenges` — WorkManager-generated daily challenges
 * - `daily_mystery` — daily "guess the country" puzzle state
 *
 * Obtained via the Hilt [DatabaseModule]; do not instantiate directly.
 */
@Database(
    entities = [
        CountryEntity::class,
        QuizSessionEntity::class,
        QuizAnswerEntity::class,
        UserPreferencesEntity::class,
        DailyChallengeEntity::class,
        DailyMysteryEntity::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = false,
)
abstract class BorderRunDatabase : RoomDatabase() {

    /** DAO for the `countries` table. */
    abstract fun countryDao(): CountryDao

    /** DAO for the `quiz_sessions` table. */
    abstract fun quizSessionDao(): QuizSessionDao

    /** DAO for the `quiz_answers` table. */
    abstract fun quizAnswerDao(): QuizAnswerDao

    /** DAO for the `user_preferences` table. */
    abstract fun userPreferencesDao(): UserPreferencesDao

    /** DAO for the `daily_challenges` table. */
    abstract fun dailyChallengeDao(): DailyChallengeDao

    /** DAO for the `daily_mystery` table. */
    abstract fun dailyMysteryDao(): DailyMysteryDao
}
