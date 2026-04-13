package com.borderrun.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Supplemental DTO for the second RestCountries API call.
 *
 * The `/all` endpoint limits requests to 10 fields. [CountryDto] covers the
 * first 10 core fields; this DTO captures the remaining fields requested in a
 * separate call. Results are merged by [cca3] in [CountryRepositoryImpl].
 *
 * Fields requested: `name,cca3,area,landlocked,car,timezones` (6 fields).
 *
 * @property cca3 Three-letter country code — join key for merging with [CountryDto].
 * @property area Land area in km². Null for some territories.
 * @property landlocked Whether the country has no sea access. Null if not returned.
 * @property car Driving-side information. Null for some territories.
 * @property timezones List of IANA timezone strings. May be empty.
 */
data class CountryExtraDto(
    @SerializedName("cca3") val cca3: String,
    @SerializedName("area") val area: Double?,
    @SerializedName("landlocked") val landlocked: Boolean?,
    @SerializedName("car") val car: CarDto?,
    @SerializedName("timezones") val timezones: List<String>?,
)
