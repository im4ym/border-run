package com.borderrun.app.data.remote.dto

import com.borderrun.app.data.local.entity.CountryEntity
import com.google.gson.annotations.SerializedName

/**
 * Nested DTO for the `name` field in a RestCountries API response.
 *
 * @property common Common short name, e.g. `"Vietnam"`.
 * @property official Official long name, e.g. `"Socialist Republic of Vietnam"`.
 */
data class NameDto(
    @SerializedName("common") val common: String,
    @SerializedName("official") val official: String,
)

/**
 * Nested DTO for the `flags` field in a RestCountries API response.
 *
 * @property png URL to the PNG flag image (~320px wide).
 * @property svg URL to the SVG flag image.
 */
data class FlagsDto(
    @SerializedName("png") val png: String,
    @SerializedName("svg") val svg: String,
)

/**
 * Nested DTO for the `car` field (driving side information).
 *
 * Shared between [CountryDto] and [CountryExtraDto].
 *
 * @property side `"left"` or `"right"`.
 */
data class CarDto(
    @SerializedName("side") val side: String,
)

/**
 * Top-level DTO for the first RestCountries API call (10 core fields).
 *
 * Fields requested:
 * `name,cca3,capital,flags,population,region,subregion,languages,currencies,borders`
 *
 * Supplemental fields (area, landlocked, car, timezones) are fetched in a
 * separate call and represented by [CountryExtraDto]. The two DTOs are merged
 * by [cca3] in [CountryRepositoryImpl] before writing to Room.
 *
 * @property name Name wrapper containing common and official names.
 * @property cca3 Three-letter country code used as the primary key.
 * @property capital List of capital city names (some countries have multiple).
 * @property flags Flag image URLs.
 * @property population Total population.
 * @property region Continental region string.
 * @property subregion Sub-continental region string.
 * @property languages Map of ISO language code → language name.
 * @property currencies Map of ISO currency code → [CurrencyDto].
 * @property borders List of bordering country cca3 codes.
 */
data class CountryDto(
    @SerializedName("name") val name: NameDto,
    @SerializedName("cca3") val cca3: String,
    @SerializedName("capital") val capital: List<String>?,
    @SerializedName("flags") val flags: FlagsDto,
    @SerializedName("population") val population: Long,
    @SerializedName("region") val region: String,
    @SerializedName("subregion") val subregion: String?,
    @SerializedName("languages") val languages: Map<String, String>?,
    @SerializedName("currencies") val currencies: Map<String, CurrencyDto>?,
    @SerializedName("borders") val borders: List<String>?,
)

/**
 * Nested DTO for currency entries in the `currencies` map.
 *
 * @property name Full currency name, e.g. `"Vietnamese đồng"`.
 * @property symbol Currency symbol, e.g. `"₫"`.
 */
data class CurrencyDto(
    @SerializedName("name") val name: String,
    @SerializedName("symbol") val symbol: String?,
)

// ── Mapping extension ─────────────────────────────────────────────────────────

/** Separator used when joining list fields into a compact JSON array string. */
private const val JSON_LIST_SEPARATOR = "\",\""

/**
 * Maps a [CountryDto] (core fields) plus an optional [CountryExtraDto]
 * (supplemental fields) to a [CountryEntity] for Room.
 *
 * Supplemental values (area, landlocked, drivingSide) are taken from [extra]
 * when available and fall back to safe defaults when the extra call result is
 * absent for a given country.
 *
 * @param cachedAt The timestamp (ms) to record as the cache write time.
 * @param extra Supplemental DTO matched by cca3, or `null` if not found.
 * @return A [CountryEntity] ready to be upserted into Room.
 */
fun CountryDto.toEntity(cachedAt: Long, extra: CountryExtraDto? = null): CountryEntity {
    val languageList = languages?.values?.toList() ?: emptyList()
    val currencyList = currencies?.values?.map { it.name } ?: emptyList()
    val borderList = borders ?: emptyList()

    return CountryEntity(
        id = cca3,
        name = name.common,
        officialName = name.official,
        capital = capital?.firstOrNull() ?: "",
        region = region,
        subregion = subregion ?: "",
        flagUrl = flags.svg,
        population = population,
        area = extra?.area ?: 0.0,
        languages = encodeJsonList(languageList),
        currencies = encodeJsonList(currencyList),
        borders = encodeJsonList(borderList),
        isLandlocked = extra?.landlocked ?: false,
        drivingSide = extra?.car?.side ?: "right",
        cachedAt = cachedAt,
    )
}

/**
 * Encodes a [List] of strings as a compact JSON array string.
 *
 * Example: `["Vietnamese", "French"]` → `["Vietnamese","French"]`
 *
 * @param items The string values to encode.
 * @return A JSON array string representation.
 */
private fun encodeJsonList(items: List<String>): String {
    if (items.isEmpty()) return "[]"
    return "[\"${items.joinToString(JSON_LIST_SEPARATOR)}\"]"
}
