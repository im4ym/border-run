package com.borderrun.app.domain.usecase

import com.borderrun.app.domain.repository.CountryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case that derives per-region country counts from the cached country list.
 *
 * The Home screen Region Grid uses these counts to show "48 countries" beneath
 * each region card.
 *
 * @property countryRepository Source of the full cached country list.
 */
class GetRegionCountsUseCase @Inject constructor(
    private val countryRepository: CountryRepository,
) {

    /**
     * Returns a [Flow] emitting a `region → count` map that updates whenever
     * the local country cache changes.
     *
     * Returns an empty map when no countries have been synced yet. Region
     * strings match the RestCountries API values (e.g. `"Asia"`, `"Europe"`,
     * `"Africa"`, `"Americas"`, `"Oceania"`).
     *
     * @return Flow emitting the region-to-count map.
     */
    operator fun invoke(): Flow<Map<String, Int>> =
        countryRepository.getAllCountries().map { countries ->
            countries.groupingBy { it.region }.eachCount()
        }
}
