package com.borderrun.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a country cached from the RestCountries API.
 *
 * List-type fields (languages, currencies, borders) are stored as JSON strings
 * to keep the schema flat. Use [com.google.gson.Gson] to serialise/deserialise.
 *
 * @property id Country code (cca3), e.g. `"VNM"`. Primary key.
 * @property name Common country name, e.g. `"Vietnam"`.
 * @property officialName Official country name, e.g. `"Socialist Republic of Vietnam"`.
 * @property capital Primary capital city name, or empty string if none.
 * @property region Continental region, e.g. `"Asia"`.
 * @property subregion Sub-continental region, e.g. `"South-Eastern Asia"`.
 * @property flagUrl PNG flag URL from flagcdn.com.
 * @property population Total population count.
 * @property area Land area in km².
 * @property languages JSON-encoded list of language names.
 * @property currencies JSON-encoded list of currency names.
 * @property borders JSON-encoded list of bordering country cca3 codes.
 * @property isLandlocked Whether the country has no sea access.
 * @property drivingSide `"left"` or `"right"`.
 * @property cachedAt Unix timestamp (ms) when this row was last written.
 */
@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val officialName: String,
    val capital: String,
    val region: String,
    val subregion: String,
    val flagUrl: String,
    val population: Long,
    val area: Double,
    val languages: String,
    val currencies: String,
    val borders: String,
    val isLandlocked: Boolean,
    val drivingSide: String,
    val cachedAt: Long,
)
