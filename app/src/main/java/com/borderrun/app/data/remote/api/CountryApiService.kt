package com.borderrun.app.data.remote.api

import com.borderrun.app.data.remote.dto.CountryDto
import retrofit2.http.GET

/** Fields requested from the RestCountries API to minimise response payload. */
private const val COUNTRY_FIELDS =
    "name,cca3,capital,flags,population,area,region,subregion,languages,currencies,borders,landlocked,car,timezones"

/**
 * Retrofit service interface for the RestCountries v3.1 API.
 *
 * Base URL: `https://restcountries.com/v3.1/`
 *
 * No authentication is required. The API has no strict rate limit, but data
 * is cached aggressively in Room to avoid unnecessary network calls.
 */
interface CountryApiService {

    /**
     * Fetches all countries with the fields required by Border Run.
     *
     * The `fields` query parameter limits the response to only the columns
     * defined in [COUNTRY_FIELDS], significantly reducing payload size.
     *
     * @return List of [CountryDto] for all ~250 countries in the world.
     */
    @GET("all?fields=$COUNTRY_FIELDS")
    suspend fun getAllCountries(): List<CountryDto>
}
