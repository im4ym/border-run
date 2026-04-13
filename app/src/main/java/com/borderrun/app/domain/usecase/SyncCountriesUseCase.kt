package com.borderrun.app.domain.usecase

import com.borderrun.app.domain.repository.CountryRepository
import javax.inject.Inject

/**
 * Result of a [SyncCountriesUseCase] invocation.
 *
 * - [Success] — API returned data and Room was updated.
 * - [Skipped] — cache is fresh (< 24 h old); no network call made.
 * - [Failed] — network call threw an exception.
 *   [Failed.cacheExists] is `true` when stale cached data is still available,
 *   allowing the UI to show cached data with a silent warning instead of a
 *   full error state.
 */
sealed class SyncResult {
    /** The API was queried and Room was updated successfully. */
    object Success : SyncResult()

    /** Cache is fresh; network was not contacted. */
    object Skipped : SyncResult()

    /**
     * Network call failed (exception thrown).
     *
     * @property cacheExists `true` when Room already contains country rows
     *   that can be shown to the user despite the sync failure.
     */
    data class Failed(val cacheExists: Boolean) : SyncResult()
}

/**
 * Syncs country data from the RestCountries API into Room.
 *
 * The sync is skipped when the cache is younger than [CACHE_MAX_AGE_MS]
 * (24 hours). On failure the use case returns [SyncResult.Failed] instead
 * of throwing, so callers can decide whether to show an error banner.
 *
 * @property countryRepository Repository used to read cache metadata and
 *   trigger the actual network fetch + upsert.
 */
class SyncCountriesUseCase @Inject constructor(
    private val countryRepository: CountryRepository,
) {

    /**
     * Executes the sync.
     *
     * @return [SyncResult.Skipped] if cache is fresh, [SyncResult.Success] on
     *   a successful refresh, or [SyncResult.Failed] if the API call throws.
     */
    suspend operator fun invoke(): SyncResult {
        return try {
            val count = countryRepository.getCachedCountryCount()
            val timestamp = countryRepository.getLatestCacheTimestamp()
            val isStale = count == 0 ||
                timestamp == null ||
                System.currentTimeMillis() - timestamp > CACHE_MAX_AGE_MS
            if (!isStale) return SyncResult.Skipped
            countryRepository.syncCountries()
            SyncResult.Success
        } catch (e: Exception) {
            val cacheExists = try {
                countryRepository.getCachedCountryCount() > 0
            } catch (_: Exception) {
                false
            }
            SyncResult.Failed(cacheExists = cacheExists)
        }
    }

    companion object {
        /** 24-hour cache TTL in milliseconds. */
        private const val CACHE_MAX_AGE_MS = 24L * 60 * 60 * 1_000
    }
}
