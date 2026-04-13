package com.borderrun.app.data.repository

import android.util.Log
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
 * function is the only code path that touches the network; it makes two
 * sequential API calls (the RestCountries `/all` endpoint limits requests to
 * 10 fields each) and merges the results by cca3 before upserting into Room.
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
     * Fetches fresh country data from two RestCountries API calls and upserts
     * the merged result into Room.
     *
     * Two calls are required because the API enforces a maximum of 10 fields
     * per request:
     * - Call 1 ([FIELDS_BASIC], 10 fields): core display data.
     * - Call 2 ([FIELDS_EXTRA], 6 fields): supplemental geographic data.
     *
     * Results are merged by cca3 before mapping to [com.borderrun.app.data.local.entity.CountryEntity].
     * Throws on network failure — the caller ([com.borderrun.app.domain.usecase.SyncCountriesUseCase])
     * handles the error and reports it to the UI.
     */
    override suspend fun syncCountries() {
        try {
            Log.d(TAG, "syncCountries: starting — call 1 ($FIELDS_BASIC)")
            val cachedAt = System.currentTimeMillis()

            val basicDtos = apiService.getAllCountriesBasic(FIELDS_BASIC)
            Log.d(TAG, "syncCountries: call 1 received ${basicDtos.size} countries")

            val extraDtos = apiService.getAllCountriesExtra(FIELDS_EXTRA)
            Log.d(TAG, "syncCountries: call 2 received ${extraDtos.size} countries")

            val extraByCode = extraDtos.associateBy { it.cca3 }
            val entities = basicDtos.map { dto -> dto.toEntity(cachedAt, extraByCode[dto.cca3]) }

            countryDao.upsertAll(entities)
            Log.d(TAG, "syncCountries: upserted ${entities.size} rows into Room")
        } catch (e: Exception) {
            Log.e(TAG, "syncCountries: failed", e)
            throw e
        }
    }

    /**
     * Returns the count of countries currently in the Room cache.
     *
     * @return Row count in the `countries` table.
     */
    override suspend fun getCachedCountryCount(): Int =
        countryDao.getCountryCount()

    /**
     * Returns the most recent cache timestamp, or `null` if the table is empty.
     *
     * @return MAX(cachedAt) from the `countries` table.
     */
    override suspend fun getLatestCacheTimestamp(): Long? =
        countryDao.getLatestCacheTimestamp()

    companion object {
        private const val TAG = "BorderRun"

        /** 10 core fields for the first API call (at the API's per-request limit). */
        private const val FIELDS_BASIC =
            "name,cca3,capital,flags,population,region,subregion,languages,currencies,borders"

        /** 6 supplemental fields for the second API call. */
        private const val FIELDS_EXTRA = "name,cca3,area,landlocked,car,timezones"
    }
}
