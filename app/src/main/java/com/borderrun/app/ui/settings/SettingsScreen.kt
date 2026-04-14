package com.borderrun.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.borderrun.app.ui.components.BottomNavTab
import com.borderrun.app.ui.components.BorderRunBottomNav
import com.borderrun.app.ui.theme.CtaGradientEnd
import com.borderrun.app.ui.theme.CtaGradientStart
import com.borderrun.app.ui.theme.DarkGradientStop1
import com.borderrun.app.ui.theme.DarkGradientStop2
import com.borderrun.app.ui.theme.DarkGradientStop3
import com.borderrun.app.ui.theme.DarkGradientStop4
import com.borderrun.app.ui.theme.ErrorRed
import com.borderrun.app.ui.theme.GradientCyan
import com.borderrun.app.ui.theme.GradientMint
import com.borderrun.app.ui.theme.GradientSky
import com.borderrun.app.ui.theme.GradientTeal
import com.borderrun.app.ui.theme.LocalIsDarkTheme
import com.borderrun.app.ui.theme.PrimaryGreen

// ── Layout constants ──────────────────────────────────────────────────────────

private val CARD_RADIUS = 22.dp
private val SECTION_SPACING = 16.dp
private val ROW_PADDING_H = 20.dp
private val ROW_PADDING_V = 16.dp

// ── Root screen ───────────────────────────────────────────────────────────────

/**
 * Root composable for the Settings screen.
 *
 * Handles permission-aware notification-toggle logic: on Android 13+ the user
 * is sent to [PermissionRationaleScreen] before the reminder is enabled; the
 * result is received back through [notificationsJustGranted] /
 * [notificationsJustDenied] (set on the NavBackStackEntry's saved state by the
 * nav graph) and consumed via the provided lambdas.
 *
 * @param onHome Navigates to the Home screen.
 * @param onQuizClick Navigates to start a new quiz.
 * @param onStats Navigates to the Statistics screen.
 * @param onNavigateToPermissionRationale Navigates to the notification permission
 *   rationale screen. Only called when the permission has not yet been granted.
 * @param notificationsJustGranted `true` for one frame after the user granted
 *   the notification permission via [PermissionRationaleScreen]. Consumed by a
 *   [LaunchedEffect] which enables the pref and schedules the worker.
 * @param notificationsJustDenied `true` for one frame after the user denied the
 *   notification permission. Consumed by a [LaunchedEffect] which shows a
 *   [Snackbar] explaining that notifications were not enabled.
 * @param onConsumeNotificationsGranted Resets [notificationsJustGranted] to
 *   `false` after it has been acted on.
 * @param onConsumeNotificationsDenied Resets [notificationsJustDenied] to
 *   `false` after it has been acted on.
 * @param viewModel Hilt-injected [SettingsViewModel].
 */
@Composable
fun SettingsScreen(
    onHome: () -> Unit,
    onQuizClick: () -> Unit,
    onStats: () -> Unit,
    onExplorer: () -> Unit,
    onNavigateToPermissionRationale: () -> Unit,
    notificationsJustGranted: Boolean,
    notificationsJustDenied: Boolean,
    onConsumeNotificationsGranted: () -> Unit,
    onConsumeNotificationsDenied: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // ── Consume permission results from PermissionRationaleScreen ─────────────
    LaunchedEffect(notificationsJustGranted) {
        if (notificationsJustGranted) {
            viewModel.setNotificationsEnabled(true)
            viewModel.scheduleReminderWorker()
            onConsumeNotificationsGranted()
        }
    }

    LaunchedEffect(notificationsJustDenied) {
        if (notificationsJustDenied) {
            snackbarHostState.showSnackbar(SNACKBAR_NOTIFICATIONS_DISABLED)
            onConsumeNotificationsDenied()
        }
    }

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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                BorderRunBottomNav(
                    currentTab = BottomNavTab.Settings,
                    onTabSelected = { tab ->
                        when (tab) {
                            BottomNavTab.Home -> onHome()
                            BottomNavTab.Quiz -> onQuizClick()
                            BottomNavTab.Stats -> onStats()
                            BottomNavTab.Settings -> Unit // already here
                            BottomNavTab.Explorer -> onExplorer()
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
                SettingsContent(
                    uiState = uiState,
                    scaffoldPadding = paddingValues,
                    onDifficultyChange = viewModel::setDifficulty,
                    onSoundChange = viewModel::setSoundEnabled,
                    onDarkModeChange = viewModel::setDarkModeEnabled,
                    onClearProgress = viewModel::clearAllProgress,
                    onNotificationsChange = { enabled ->
                        handleNotificationToggle(
                            context = context,
                            enabled = enabled,
                            onEnable = {
                                viewModel.setNotificationsEnabled(true)
                                viewModel.scheduleReminderWorker()
                            },
                            onDisable = {
                                viewModel.setNotificationsEnabled(false)
                                viewModel.cancelReminderWorker()
                            },
                            onNeedPermission = onNavigateToPermissionRationale,
                        )
                    },
                )
            }
        }
    }
}

// ── Permission helper ─────────────────────────────────────────────────────────

/**
 * Decides what action to take when the daily-reminder toggle is flipped.
 *
 * On Android 13+ (API 33), [POST_NOTIFICATIONS][android.Manifest.permission.POST_NOTIFICATIONS]
 * must be granted before sending notifications. If not yet granted, the
 * [onNeedPermission] callback sends the user to the rationale screen.
 *
 * On Android < 13 the permission is not required, so the toggle enables
 * directly.
 *
 * Must be called from the composable scope so that [context] (obtained via
 * [LocalContext.current] in the caller) is available without requiring this
 * function to be a composable itself.
 *
 * @param context Application context used for permission checks.
 * @param enabled Whether the toggle is being turned on or off.
 * @param onEnable Called when notifications should be enabled.
 * @param onDisable Called when notifications should be disabled.
 * @param onNeedPermission Called when the permission must be requested first.
 */
private fun handleNotificationToggle(
    context: android.content.Context,
    enabled: Boolean,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onNeedPermission: () -> Unit,
) {
    if (!enabled) {
        onDisable()
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) onEnable() else onNeedPermission()
    } else {
        // API < 33: POST_NOTIFICATIONS is not a runtime permission
        onEnable()
    }
}

// ── Settings content ──────────────────────────────────────────────────────────

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    scaffoldPadding: PaddingValues,
    onDifficultyChange: (String) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onClearProgress: () -> Unit,
) {
    var showClearConfirmation by remember { mutableStateOf(false) }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = statusBarPadding + 24.dp,
            bottom = scaffoldPadding.calculateBottomPadding() + 8.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        item { SectionHeader(text = "Game") }
        item {
            SettingsGlassCard {
                Column {
                    DifficultyRow(selected = uiState.difficulty, onSelect = onDifficultyChange)
                    CardDivider()
                    ToggleRow(
                        label = "Sound Effects",
                        checked = uiState.soundEnabled,
                        onCheckedChange = onSoundChange,
                    )
                }
            }
        }

        item { SectionHeader(text = "Notifications") }
        item {
            SettingsGlassCard {
                ToggleRow(
                    label = "Daily Reminder",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = onNotificationsChange,
                )
            }
        }

        item { SectionHeader(text = "Appearance") }
        item {
            SettingsGlassCard {
                ToggleRow(
                    label = "Dark Mode",
                    checked = uiState.darkModeEnabled,
                    onCheckedChange = onDarkModeChange,
                )
            }
        }

        item { SectionHeader(text = "Data") }
        item {
            SettingsGlassCard {
                Column {
                    ClearProgressRow(onClick = { showClearConfirmation = true })
                    CardDivider()
                    AboutRow()
                }
            }
        }
    }

    if (showClearConfirmation) {
        ClearProgressDialog(
            onConfirm = {
                onClearProgress()
                showClearConfirmation = false
            },
            onDismiss = { showClearConfirmation = false },
        )
    }
}

// ── Section: Game ─────────────────────────────────────────────────────────────

/**
 * Row containing the difficulty label and three chip buttons (Easy / Medium / Hard).
 *
 * The currently [selected] chip is highlighted with the CTA gradient; others
 * use the card border colour as their background.
 *
 * @param selected Current difficulty value: `"easy"`, `"medium"`, or `"hard"`.
 * @param onSelect Callback invoked when the user taps a chip.
 */
@Composable
private fun DifficultyRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ROW_PADDING_H, vertical = ROW_PADDING_V),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Difficulty",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DifficultyChip(label = "Easy", isSelected = selected == "easy", onClick = { onSelect("easy") })
            DifficultyChip(label = "Medium", isSelected = selected == "medium", onClick = { onSelect("medium") })
            DifficultyChip(label = "Hard", isSelected = selected == "hard", onClick = { onSelect("hard") })
        }
    }
}

/**
 * Single difficulty chip button.
 *
 * Selected: CTA gradient background + white bold text.
 * Unselected: translucent [MaterialTheme.colorScheme.outline] background + [TextBody] colour.
 *
 * @param label Display text e.g. `"Easy"`.
 * @param isSelected Whether this chip represents the active difficulty.
 * @param onClick Invoked when the chip is tapped.
 */
@Composable
private fun DifficultyChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val background: Brush = if (isSelected) {
        Brush.horizontalGradient(colors = listOf(CtaGradientStart, CtaGradientEnd))
    } else {
        Brush.horizontalGradient(colors = listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
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

// ── Data section rows ─────────────────────────────────────────────────────────

/**
 * Tappable row that triggers the Clear Progress confirmation dialog.
 *
 * The label is rendered in [ErrorRed] to signal a destructive action.
 *
 * @param onClick Invoked when the row is tapped.
 */
@Composable
private fun ClearProgressRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ROW_PADDING_H, vertical = ROW_PADDING_V),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Clear Progress",
            style = MaterialTheme.typography.bodyLarge,
            color = ErrorRed,
            fontWeight = FontWeight.SemiBold,
        )
        Text(text = "🗑️", fontSize = 18.sp)
    }
}

/** Static row displaying the app name and version number. */
@Composable
private fun AboutRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ROW_PADDING_H, vertical = ROW_PADDING_V),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "About",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Border Run v$APP_VERSION",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Clear progress dialog ─────────────────────────────────────────────────────

/**
 * Confirmation dialog shown before wiping all quiz progress.
 *
 * @param onConfirm Invoked when the user taps "Clear" to proceed.
 * @param onDismiss Invoked when the user taps "Cancel" or dismisses the dialog.
 */
@Composable
private fun ClearProgressDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Clear Progress?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "This will permanently delete all your quiz sessions and results. This cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Clear", color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

// ── Shared components ─────────────────────────────────────────────────────────

/**
 * Reusable glassmorphism card container used for each settings section.
 *
 * @param content Composable content placed inside the card.
 */
@Composable
private fun SettingsGlassCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = { content() },
    )
}

/**
 * Bold section header displayed above each settings card.
 *
 * @param text Section title string.
 */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold,
    )
}

/**
 * A label-on-left, [Switch]-on-right preference row.
 *
 * @param label Human-readable preference label.
 * @param checked Current toggle state.
 * @param onCheckedChange Callback invoked when the user flips the switch.
 */
@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ROW_PADDING_H, vertical = ROW_PADDING_V),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

/** Thin horizontal divider used to separate rows inside a settings card. */
@Composable
private fun CardDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outline),
    )
}

// ── Constants ─────────────────────────────────────────────────────────────────

/** Display version shown in the About row. */
private const val APP_VERSION = "1.0"

/** Snackbar message shown when the notification permission was denied. */
private const val SNACKBAR_NOTIFICATIONS_DISABLED =
    "Notifications disabled. You can enable them in your device Settings."
