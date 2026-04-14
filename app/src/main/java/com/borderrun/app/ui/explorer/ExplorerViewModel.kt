package com.borderrun.app.ui.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borderrun.app.domain.repository.CountryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the Explorer screen.
 *
 * Combines the full cached country list from [CountryRepository] with the
 * current search query and region filter to produce a live [ExplorerUiState].
 * Filtering is performed in the `combine` transform so Room changes are
 * reflected automatically without a manual refresh.
 *
 * @property countryRepository Provides the cached country list as a [Flow].
 */
@HiltViewModel
class ExplorerViewModel @Inject constructor(
    private val countryRepository: CountryRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedRegion = MutableStateFlow<String?>(null)
    private val _expandedCountryId = MutableStateFlow<String?>(null)

    /**
     * Unified Explorer screen state.
     *
     * Derives [ExplorerUiState.countries] by:
     * 1. Filtering by [_selectedRegion] when non-null.
     * 2. Filtering by [_searchQuery] against name and capital (case-insensitive).
     * 3. Sorting the result alphabetically by country name.
     */
    val uiState: StateFlow<ExplorerUiState> = combine(
        countryRepository.getAllCountries(),
        _searchQuery,
        _selectedRegion,
        _expandedCountryId,
    ) { countries, query, region, expandedId ->
        val filtered = countries
            .filter { country ->
                (region == null || country.region == region) &&
                    (query.isBlank() ||
                        country.name.contains(query, ignoreCase = true) ||
                        country.capital.contains(query, ignoreCase = true))
            }
            .sortedBy { it.name }
        ExplorerUiState(
            isLoading = false,
            countries = filtered,
            searchQuery = query,
            selectedRegion = region,
            expandedCountryId = expandedId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = ExplorerUiState(isLoading = true),
    )

    // ── User actions ──────────────────────────────────────────────────────────

    /** Updates the search query; filtering is applied immediately. */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Sets the active region filter.
     *
     * @param region The region name to filter by, or `null` to show all regions.
     */
    fun onRegionFilterChange(region: String?) {
        _selectedRegion.value = region
    }

    /**
     * Toggles the expanded state of a country card.
     *
     * Tapping an already-expanded card collapses it (sets [ExplorerUiState.expandedCountryId]
     * to `null`); tapping a different card replaces the expanded entry.
     *
     * @param countryId The [com.borderrun.app.domain.model.Country.id] of the tapped card.
     */
    fun onToggleCountryExpanded(countryId: String) {
        _expandedCountryId.value =
            if (_expandedCountryId.value == countryId) null else countryId
    }

    companion object {
        private const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
