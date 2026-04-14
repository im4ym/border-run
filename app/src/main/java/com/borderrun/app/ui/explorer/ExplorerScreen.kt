package com.borderrun.app.ui.explorer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.borderrun.app.domain.model.Country
import com.borderrun.app.ui.components.BottomNavTab
import com.borderrun.app.ui.components.BorderRunBottomNav
import com.borderrun.app.ui.theme.CtaGradientEnd
import com.borderrun.app.ui.theme.CtaGradientStart
import com.borderrun.app.ui.theme.DarkGradientStop1
import com.borderrun.app.ui.theme.DarkGradientStop2
import com.borderrun.app.ui.theme.DarkGradientStop3
import com.borderrun.app.ui.theme.DarkGradientStop4
import com.borderrun.app.ui.theme.GradientCyan
import com.borderrun.app.ui.theme.GradientMint
import com.borderrun.app.ui.theme.GradientSky
import com.borderrun.app.ui.theme.GradientTeal
import com.borderrun.app.ui.theme.LocalIsDarkTheme
import com.borderrun.app.ui.theme.PrimaryGreen

// ── Region filter data ────────────────────────────────────────────────────────

/** Region chips shown in the Explorer filter row; `null` = "All". */
private val FILTER_REGIONS: List<String?> =
    listOf(null, "Asia", "Europe", "Africa", "Americas", "Oceania", "Antarctic")

private fun regionLabel(region: String?): String = region ?: "All"

private fun regionEmoji(region: String?): String = when (region) {
    "Asia" -> "🌏"
    "Europe" -> "🌍"
    "Africa" -> "☀️"
    "Americas" -> "🌎"
    "Oceania" -> "🌊"
    "Antarctic" -> "🧊"
    else -> "🌐"
}

// ── Root screen ───────────────────────────────────────────────────────────────

/**
 * Root composable for the Explorer screen.
 *
 * Provides a search bar, horizontal region filter chips, and a lazy scrollable
 * list of country cards. Tapping a card expands it inline to reveal extra
 * details (area, languages, currencies, driving side, borders).
 *
 * @param onHome Navigates to the Home screen.
 * @param onQuizClick Navigates to a mixed quiz.
 * @param onStats Navigates to the Statistics screen.
 * @param onSettings Navigates to the Settings screen.
 * @param viewModel Hilt-injected [ExplorerViewModel].
 */
@Composable
fun ExplorerScreen(
    onHome: () -> Unit,
    onQuizClick: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    viewModel: ExplorerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val isDark = LocalIsDarkTheme.current
    val gradient = Brush.verticalGradient(
        colors = if (isDark) listOf(DarkGradientStop1, DarkGradientStop2, DarkGradientStop3, DarkGradientStop4)
                 else listOf(GradientMint, GradientTeal, GradientCyan, GradientSky),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BorderRunBottomNav(
                    currentTab = BottomNavTab.Explorer,
                    onTabSelected = { tab ->
                        when (tab) {
                            BottomNavTab.Home -> onHome()
                            BottomNavTab.Quiz -> onQuizClick()
                            BottomNavTab.Stats -> onStats()
                            BottomNavTab.Settings -> onSettings()
                            BottomNavTab.Explorer -> Unit // already here
                        }
                    },
                )
            },
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CtaGradientStart)
                }
            } else {
                ExplorerContent(
                    uiState = uiState,
                    scaffoldPadding = paddingValues,
                    onSearchChange = viewModel::onSearchQueryChange,
                    onRegionFilterChange = viewModel::onRegionFilterChange,
                    onToggleExpanded = viewModel::onToggleCountryExpanded,
                )
            }
        }
    }
}

// ── Explorer content ──────────────────────────────────────────────────────────

@Composable
private fun ExplorerContent(
    uiState: ExplorerUiState,
    scaffoldPadding: PaddingValues,
    onSearchChange: (String) -> Unit,
    onRegionFilterChange: (String?) -> Unit,
    onToggleExpanded: (String) -> Unit,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = statusBarPadding + 16.dp,
            bottom = scaffoldPadding.calculateBottomPadding() + 8.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Title
        item {
            Text(
                text = "Explore Countries",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${uiState.countries.size} countries",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
        }

        // Search bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                label = { Text("Search by name or capital…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                ),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
        }

        // Region filter chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(FILTER_REGIONS) { region ->
                    RegionChip(
                        label = "${regionEmoji(region)} ${regionLabel(region)}",
                        isSelected = uiState.selectedRegion == region,
                        onClick = { onRegionFilterChange(region) },
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        // Country cards
        if (uiState.countries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No countries match your search.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            items(uiState.countries, key = { it.id }) { country ->
                CountryCard(
                    country = country,
                    isExpanded = uiState.expandedCountryId == country.id,
                    onToggle = { onToggleExpanded(country.id) },
                )
            }
        }
    }
}

// ── Region filter chip ────────────────────────────────────────────────────────

@Composable
private fun RegionChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val background: Brush = if (isSelected) {
        Brush.horizontalGradient(listOf(CtaGradientStart, CtaGradientEnd))
    } else {
        Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

// ── Country card ──────────────────────────────────────────────────────────────

/**
 * Collapsible card showing a country's flag, name, capital, region and (when
 * expanded) detailed information.
 *
 * @param country The country to display.
 * @param isExpanded Whether the detail section is currently visible.
 * @param onToggle Called when the card header row is tapped.
 */
@Composable
private fun CountryCard(country: Country, isExpanded: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, if (isExpanded) PrimaryGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        // ── Header row (always visible) ───────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                model = country.flagUrl,
                contentDescription = "${country.name} flag",
                modifier = Modifier
                    .size(width = 56.dp, height = 38.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = buildString {
                        if (country.capital.isNotBlank()) append("🏛 ${country.capital}  ")
                        append("• ${country.region}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = country.population.formatPop(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Expanded detail section ───────────────────────────────────────────
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DetailDivider()
                DetailRow(label = "Official Name", value = country.officialName)
                DetailRow(label = "Subregion", value = country.subregion.ifBlank { "—" })
                DetailRow(label = "Population", value = country.population.formatPopLong())
                DetailRow(label = "Area", value = "${"%,.0f".format(country.area)} km²")
                DetailRow(
                    label = "Languages",
                    value = country.languages.take(4).joinToString(", ").ifBlank { "—" },
                )
                DetailRow(
                    label = "Currencies",
                    value = country.currencies.take(3).joinToString(", ").ifBlank { "—" },
                )
                DetailRow(label = "Landlocked", value = if (country.isLandlocked) "Yes" else "No")
                DetailRow(label = "Driving Side", value = country.drivingSide.replaceFirstChar { it.uppercase() })
                DetailRow(
                    label = "Borders",
                    value = if (country.borders.isEmpty()) "None (island or enclave)"
                    else "${country.borders.size}: ${country.borders.take(6).joinToString(", ")}${if (country.borders.size > 6) "…" else ""}",
                )
            }
        }
    }
}

@Composable
private fun DetailDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outline),
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}

// ── Format helpers ────────────────────────────────────────────────────────────

/** Short compact population label (e.g. "83M", "4.5B"). */
private fun Long.formatPop(): String = when {
    this >= 1_000_000_000L -> "${"%.1f".format(this / 1_000_000_000.0)}B"
    this >= 1_000_000L -> "${this / 1_000_000}M"
    this >= 1_000L -> "${this / 1_000}K"
    else -> toString()
}

/** Full population string for the expanded detail row (e.g. "83,240,525"). */
private fun Long.formatPopLong(): String = "%,d".format(this)
