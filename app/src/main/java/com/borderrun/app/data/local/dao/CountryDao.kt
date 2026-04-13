package com.borderrun.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.borderrun.app.data.local.entity.CountryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `countries` table.
 *
 * All reads return [Flow] so the UI layer reacts automatically to database
 * changes without manual refresh calls.
 */
@Dao
interface CountryDao {

    /**
     * Inserts or replaces a list of [CountryEntity] rows.
     *
     * Used by [ContentSyncWorker] after fetching fresh data from the
     * RestCountries API.
     *
     * @param countries The list of country rows to upsert.
     */
    @Upsert
    suspend fun upsertAll(countries: List<CountryEntity>)

    /**
     * Returns all cached countries as a reactive [Flow].
     *
     * @return Flow emitting the full country list whenever the table changes.
     */
    @Query("SELECT * FROM countries ORDER BY name ASC")
    fun getAllCountries(): Flow<List<CountryEntity>>

    /**
     * Returns all countries belonging to [region].
     *
     * @param region Continental region filter, e.g. `"Asia"`.
     * @return Flow emitting matching countries sorted by name.
     */
    @Query("SELECT * FROM countries WHERE region = :region ORDER BY name ASC")
    fun getCountriesByRegion(region: String): Flow<List<CountryEntity>>

    /**
     * Returns a single country by its cca3 [id].
     *
     * @param id Country code, e.g. `"VNM"`.
     * @return Flow emitting the matching [CountryEntity] or `null` if not found.
     */
    @Query("SELECT * FROM countries WHERE id = :id")
    fun getCountryById(id: String): Flow<CountryEntity?>

    /**
     * Returns all countries that are landlocked.
     *
     * @return Flow emitting landlocked countries sorted by name.
     */
    @Query("SELECT * FROM countries WHERE isLandlocked = 1 ORDER BY name ASC")
    fun getLandlockedCountries(): Flow<List<CountryEntity>>

    /**
     * Returns the number of countries currently cached.
     *
     * @return Total row count in the `countries` table.
     */
    @Query("SELECT COUNT(*) FROM countries")
    suspend fun getCountryCount(): Int

    /**
     * Returns the most recent [CountryEntity.cachedAt] timestamp, or `null`
     * if the table is empty. Used to decide whether a refresh is needed.
     *
     * @return Most recent cache timestamp in milliseconds, or `null`.
     */
    @Query("SELECT MAX(cachedAt) FROM countries")
    suspend fun getLatestCacheTimestamp(): Long?

    /**
     * Deletes all rows from the `countries` table.
     *
     * Called from "Clear All My Data" in Settings.
     */
    @Query("DELETE FROM countries")
    suspend fun deleteAll()
}
