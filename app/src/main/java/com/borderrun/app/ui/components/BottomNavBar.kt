package com.borderrun.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MaterialTheme
import com.borderrun.app.ui.theme.PrimaryGreen

/**
 * Enumerates the four tabs in the Border Run bottom navigation bar.
 *
 * Each entry holds the display [label] and the Material [icon] used to render
 * the [NavigationBarItem].
 *
 * @property label Human-readable tab name shown beneath the icon.
 * @property icon Material icon vector for the tab.
 */
enum class BottomNavTab(val label: String, val icon: ImageVector) {
    /** Landing / Home screen tab. */
    Home(label = "Home", icon = Icons.Default.Home),

    /** Quiz entry point tab — navigates to the region/difficulty selector. */
    Quiz(label = "Quiz", icon = Icons.Default.PlayArrow),

    /** Statistics screen tab. */
    Stats(label = "Stats", icon = Icons.Default.Star),

    /** Settings screen tab. */
    Settings(label = "Settings", icon = Icons.Default.Settings),

    /** Explorer screen tab — browse and search all countries. */
    Explorer(label = "Explore", icon = Icons.Default.Search),
}

/**
 * Shared bottom navigation bar for all primary screens (Home, Stats, Settings).
 *
 * Renders four [NavigationBarItem] entries from [BottomNavTab]. The active tab
 * is highlighted in [PrimaryGreen]; inactive labels use [TextBody].
 *
 * The [NavigationBar] uses a semi-transparent [CardSurface] background to
 * blend with the app's gradient background.
 *
 * @param currentTab The currently selected [BottomNavTab] — its item is shown
 *   as selected.
 * @param onTabSelected Callback invoked when the user taps a tab. Not called
 *   for the already-selected tab.
 */
@Composable
fun BorderRunBottomNav(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        BottomNavTab.entries.forEach { tab ->
            val selected = tab == currentTab
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(text = tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    indicatorColor = MaterialTheme.colorScheme.surface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}
