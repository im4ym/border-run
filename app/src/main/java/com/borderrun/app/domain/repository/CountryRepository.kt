package com.borderrun.app.domain.repository

import com.borderrun.app.domain.model.Country
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for country data, defined in the domain layer.
 *
 * The domain layer depends only on this interface; the concrete implementation
 * ([com.borderrun.app.data.repository.CountryRepositoryImpl]) lives in the
 * data layer and is provided via Hilt [RepositoryModule].
 *
 * Offline-first contract:
 * - All [Flow]-returning functions read from the local Room cache.
 * - [syncCountries] is called by [ContentSyncWorker] to refresh the cache.
 */
interface CountryRepository {

    /**
     * Returns all cached countries as a reactive [Flow].
     *
     * @return Flow emitting the full sorted list of [Country] domain models.
     */
    fun getAllCountries(): Flow<List<Country>>

    /**
     * Returns all countries for the given [region] as a reactive [Flow].
     *
     * @param region Continental region filter, e.g. `"Asia"`.
     * @return Flow emitting countries in the specified region, sorted by name.
     */
    fun getCountriesByRegion(region: String): Flow<List<Country>>

    /**
     * Returns a single country by its cca3 [id] as a reactive [Flow].
     *
     * @param id cca3 country code, e.g. `"VNM"`.
     * @return Flow emitting the matching [Country], or `null` if not cached.
     */
    fun getCountryById(id: String): Flow<Country?>

    /**
     * Fetches fresh country data from the RestCountries API and upserts it
     * into the local Room database.
     *
     * Should only be called from [ContentSyncWorker] or on explicit user
     * refresh. Throws an exception if the network request fails — callers
     * should catch and fall back to cached data.
     */
    suspend fun syncCountries()

    /**
     * Returns the number of countries currently cached in Room.
     *
     * Used by the Settings screen to display cache stats.
     *
     * @return Total cached country count.
     */
    suspend fun getCachedCountryCount(): Int
}
