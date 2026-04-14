package com.borderrun.app.ui.explorer

import com.borderrun.app.domain.model.Country

/**
 * UI state for the Explorer screen.
 *
 * @property isLoading `true` while the country list is being fetched from Room.
 * @property countries Filtered and sorted list of countries currently shown.
 * @property searchQuery Current text in the search field.
 * @property selectedRegion Active region filter, or `null` for "All regions".
 * @property expandedCountryId The [Country.id] of the currently expanded country
 *   card, or `null` if none are expanded.
 */
data class ExplorerUiState(
    val isLoading: Boolean = true,
    val countries: List<Country> = emptyList(),
    val searchQuery: String = "",
    val selectedRegion: String? = null,
    val expandedCountryId: String? = null,
)
