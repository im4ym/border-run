package com.borderrun.app.domain.model

import com.borderrun.app.data.local.entity.CountryEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Domain model representing a country used throughout the UI and use-case layers.
 *
 * This is a pure Kotlin data class with no Android or Room dependencies. It is
 * mapped from [CountryEntity] via [CountryEntity.toDomain].
 *
 * @property id cca3 country code, e.g. `"VNM"`.
 * @property name Common name, e.g. `"Vietnam"`.
 * @property officialName Official name, e.g. `"Socialist Republic of Vietnam"`.
 * @property capital Primary capital city, or empty string if none.
 * @property region Continental region, e.g. `"Asia"`.
 * @property subregion Sub-continental region, e.g. `"South-Eastern Asia"`.
 * @property flagUrl PNG flag URL.
 * @property population Total population.
 * @property area Land area in km².
 * @property languages Decoded list of spoken language names.
 * @property currencies Decoded list of currency names.
 * @property borders Decoded list of bordering country cca3 codes.
 * @property isLandlocked Whether the country has no sea access.
 * @property drivingSide `"left"` or `"right"`.
 */
data class Country(
    val id: String,
    val name: String,
    val officialName: String,
    val capital: String,
    val region: String,
    val subregion: String,
    val flagUrl: String,
    val population: Long,
    val area: Double,
    val languages: List<String>,
    val currencies: List<String>,
    val borders: List<String>,
    val isLandlocked: Boolean,
    val drivingSide: String,
)

// ── Entity → Domain mapping ───────────────────────────────────────────────────

/** Reusable Gson instance for decoding JSON-list columns. */
private val gson = Gson()

/** Type token for deserialising `List<String>` from a JSON string column. */
private val stringListType = object : TypeToken<List<String>>() {}.type

/**
 * Maps a [CountryEntity] from Room to a [Country] domain model.
 *
 * JSON-encoded string columns (languages, currencies, borders) are decoded
 * back into typed lists using Gson.
 *
 * @return The [Country] domain model equivalent of this entity.
 */
fun CountryEntity.toDomain(): Country = Country(
    id = id,
    name = name,
    officialName = officialName,
    capital = capital,
    region = region,
    subregion = subregion,
    flagUrl = flagUrl,
    population = population,
    area = area,
    languages = decodeJsonList(languages),
    currencies = decodeJsonList(currencies),
    borders = decodeJsonList(borders),
    isLandlocked = isLandlocked,
    drivingSide = drivingSide,
)

/**
 * Decodes a JSON-encoded string list back into a [List] of strings.
 *
 * Returns an empty list for `"[]"` or on any parse error.
 *
 * @param json A JSON array string, e.g. `["Vietnamese","French"]`.
 * @return Decoded list, or empty list on failure.
 */
private fun decodeJsonList(json: String): List<String> = try {
    gson.fromJson(json, stringListType) ?: emptyList()
} catch (e: Exception) {
    emptyList()
}
