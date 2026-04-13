package com.borderrun.app.data.repository

import com.borderrun.app.data.local.dao.CountryDao
import com.borderrun.app.data.remote.api.CountryApiService
import com.borderrun.app.data.remote.dto.toEntity
import com.borderrun.app.domain.model.Country
import com.borderrun.app.domain.model.toDomain
import com.borderrun.app.domain.repository.CountryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first implementation of [CountryRepository].
 *
 * Reads always serve from the local [CountryDao] (Room). The [syncCountries]
 * function is the only code path that touches the network; it is invoked
 * exclusively by [ContentSyncWorker].
 *
 * @property countryDao Room DAO for the `countries` table.
 * @property apiService Retrofit service for the RestCountries API.
 */
class CountryRepositoryImpl @Inject constructor(
    private val countryDao: CountryDao,
    private val apiService: CountryApiService,
) : CountryRepository {

    /**
     * Returns all cached countries as a [Flow] of domain models.
     *
     * @return Flow emitting the full sorted [Country] list on every DB change.
     */
    override fun getAllCountries(): Flow<List<Country>> =
        countryDao.getAllCountries().map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Returns cached countries for the given [region].
     *
     * @param region Continental region filter, e.g. `"Asia"`.
     */
    override fun getCountriesByRegion(region: String): Flow<List<Country>> =
        countryDao.getCountriesByRegion(region).map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Returns a single cached country by cca3 [id].
     *
     * @param id cca3 country code.
     */
    override fun getCountryById(id: String): Flow<Country?> =
        countryDao.getCountryById(id).map { entity ->
            entity?.toDomain()
        }

    /**
     * Fetches fresh country data from the RestCountries API and upserts all
     * rows into Room.
     *
     * Records the current system time as [cachedAt] for cache invalidation.
     * Throws on network failure — the caller ([ContentSyncWorker]) handles
     * retries via WorkManager's back-off policy.
     */
    override suspend fun syncCountries() {
        val cachedAt = System.currentTimeMillis()
        val dtos = apiService.getAllCountries()
        val entities = dtos.map { it.toEntity(cachedAt) }
        countryDao.upsertAll(entities)
    }

    /**
     * Returns the count of countries currently in the Room cache.
     *
     * @return Row count in the `countries` table.
     */
    override suspend fun getCachedCountryCount(): Int =
        countryDao.getCountryCount()
}
