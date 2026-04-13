package com.borderrun.app.data.remote.api

import com.borderrun.app.data.remote.dto.CountryDto
import com.borderrun.app.data.remote.dto.CountryExtraDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for the RestCountries v3.1 API.
 *
 * Base URL: `https://restcountries.com/`
 *
 * The `/all` endpoint enforces a maximum of 10 fields per request, so country
 * data is fetched in two calls and merged by cca3 in [CountryRepositoryImpl]:
 *
 * - [getAllCountriesBasic] — 10 core fields (name, cca3, capital, flags,
 *   population, region, subregion, languages, currencies, borders)
 * - [getAllCountriesExtra] — 6 supplemental fields (name, cca3, area,
 *   landlocked, car, timezones)
 */
interface CountryApiService {

    /**
     * Fetches core fields for all countries (10 fields, at the API limit).
     *
     * @param fields Comma-separated field names — must be exactly 10 or fewer.
     * @return List of [CountryDto] for all ~250 countries.
     */
    @GET("v3.1/all")
    suspend fun getAllCountriesBasic(
        @Query("fields") fields: String,
    ): List<CountryDto>

    /**
     * Fetches supplemental fields for all countries (6 fields).
     *
     * @param fields Comma-separated field names — must be exactly 10 or fewer.
     * @return List of [CountryExtraDto] for all ~250 countries.
     */
    @GET("v3.1/all")
    suspend fun getAllCountriesExtra(
        @Query("fields") fields: String,
    ): List<CountryExtraDto>
}
