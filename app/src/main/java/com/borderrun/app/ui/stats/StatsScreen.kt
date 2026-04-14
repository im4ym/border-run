package com.borderrun.app.ui.stats

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.borderrun.app.ui.theme.RegionAfricaGradientStart
import com.borderrun.app.ui.theme.RegionAfricaPrimary
import com.borderrun.app.ui.theme.RegionAmericasGradientStart
import com.borderrun.app.ui.theme.RegionAmericasPrimary
import com.borderrun.app.ui.theme.RegionAsiaGradientStart
import com.borderrun.app.ui.theme.RegionAsiaPrimary
import com.borderrun.app.ui.theme.RegionEuropeGradientStart
import com.borderrun.app.ui.theme.RegionEuropePrimary
import com.borderrun.app.ui.theme.RegionOceaniaPrimary
import com.borderrun.app.ui.theme.TextBody
import com.borderrun.app.ui.theme.TextHeading
import com.borderrun.app.ui.theme.TextMuted
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// ── Layout constants ──────────────────────────────────────────────────────────

private val CARD_RADIUS = 22.dp
private val STAT_CARD_RADIUS = 16.dp
private val SECTION_SPACING = 16.dp
private val CARD_PADDING = 20.dp
private val BAR_HEIGHT = 10.dp
private val BAR_RADIUS = 12.dp

/** Regions shown in the Accuracy by Region section, in display order. */
private data class RegionBarConfig(
    val name: String,
    val emoji: String,
    val gradientStart: Color,
    val primary: Color,
)

private val REGION_BARS = listOf(
    RegionBarConfig("Asia", "🌏", RegionAsiaGradientStart, RegionAsiaPrimary),
    RegionBarConfig("Europe", "🌍", RegionEuropeGradientStart, RegionEuropePrimary),
    RegionBarConfig("Africa", "☀️", RegionAfricaGradientStart, RegionAfricaPrimary),
    RegionBarConfig("Americas", "🌎", RegionAmericasGradientStart, RegionAmericasPrimary),
)

private val MONTH_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

// ── Root screen ───────────────────────────────────────────────────────────────

/**
 * Root composable for the Statistics screen.
 *
 * Collects [StatsViewModel.uiState] and renders either a loading spinner, an
 * empty state (no quizzes played), or the full statistics layout.
 *
 * @param onHome Navigates to the Home screen.
 * @param onQuizClick Navigates to start a new quiz.
 * @param onSettings Navigates to the Settings screen.
 * @param viewModel Hilt-injected [StatsViewModel].
 */
@Composable
fun StatsScreen(
    onHome: () -> Unit,
    onQuizClick: () -> Unit,
    onSettings: () -> Unit,
    onExplorer: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
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
                    currentTab = BottomNavTab.Stats,
                    onTabSelected = { tab ->
                        when (tab) {
                            BottomNavTab.Home -> onHome()
                            BottomNavTab.Quiz -> onQuizClick()
                            BottomNavTab.Settings -> onSettings()
                            BottomNavTab.Stats -> Unit // already here
                            BottomNavTab.Explorer -> onExplorer()
                        }
                    },
                )
            },
        ) { paddingValues ->
            when {
                uiState.isLoading -> StatsLoadingContent()
                uiState.totalQuizzes == 0 -> StatsEmptyContent(
                    scaffoldPadding = paddingValues,
                    onStartQuiz = onQuizClick,
                )
                else -> StatsSuccessContent(
                    uiState = uiState,
                    scaffoldPadding = paddingValues,
                )
            }
        }
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
private fun StatsLoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CtaGradientStart)
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun StatsEmptyContent(
    scaffoldPadding: PaddingValues,
    onStartQuiz: () -> Unit,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = statusBarPadding,
                bottom = scaffoldPadding.calculateBottomPadding(),
            )
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = "🌍", fontSize = 64.sp)
            Text(
                text = "No quizzes yet!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Start playing to see your stats here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(CtaGradientStart, CtaGradientEnd)))
                    .clickable(onClick = onStartQuiz),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Start a Quiz",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

// ── Success layout ────────────────────────────────────────────────────────────

@Composable
private fun StatsSuccessContent(
    uiState: StatsUiState,
    scaffoldPadding: PaddingValues,
) {
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
        // ── Page title ────────────────────────────────────────────────────
        item {
            Text(
                text = "Your Statistics",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        // ── Overview card ─────────────────────────────────────────────────
        item {
            OverviewCard(uiState = uiState)
        }

        // ── Streak / time / avg score row ─────────────────────────────────
        item {
            StatsRow(uiState = uiState)
        }

        // ── Accuracy by region ────────────────────────────────────────────
        item {
            SectionHeader(text = "Accuracy by Region")
        }
        item {
            RegionAccuracyCard(regionAccuracies = uiState.regionAccuracies)
        }

        // ── Recent quizzes ────────────────────────────────────────────────
        item {
            SectionHeader(text = "Recent Quizzes")
        }
        items(uiState.recentSessions) { session ->
            RecentQuizRow(session = session)
        }
    }
}

// ── Overview card ─────────────────────────────────────────────────────────────

/**
 * Glass card displaying the three primary headline stats: total quizzes,
 * questions answered, and overall accuracy percentage.
 *
 * @param uiState Source of the headline stat values.
 */
@Composable
private fun OverviewCard(uiState: StatsUiState) {
    StatsGlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            OverviewStatColumn(
                label = "Quizzes",
                value = "${uiState.totalQuizzes}",
                emoji = "🎯",
                modifier = Modifier.weight(1f),
            )
            StatDivider()
            OverviewStatColumn(
                label = "Questions",
                value = "${uiState.questionsAnswered}",
                emoji = "📝",
                modifier = Modifier.weight(1f),
            )
            StatDivider()
            OverviewStatColumn(
                label = "Accuracy",
                value = "${uiState.overallAccuracyPercent}%",
                emoji = "✅",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Single column inside the overview card showing one stat with an emoji. */
@Composable
private fun OverviewStatColumn(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = emoji, fontSize = 22.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Thin vertical divider used to separate overview stat columns. */
@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(60.dp)
            .background(MaterialTheme.colorScheme.outline),
    )
}

// ── Stats row ─────────────────────────────────────────────────────────────────

/**
 * Row of three mini-cards showing streak, total time played, and average score.
 *
 * @param uiState Source of streak, time, and score values.
 */
@Composable
private fun StatsRow(uiState: StatsUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MiniStatCard(
            emoji = "🔥",
            label = "Streak",
            value = "${uiState.currentStreak}d",
            modifier = Modifier.weight(1f),
        )
        MiniStatCard(
            emoji = "⏱",
            label = "Time Played",
            value = uiState.totalTimePlayedMs.formatTotalTime(),
            modifier = Modifier.weight(1f),
        )
        MiniStatCard(
            emoji = "⭐",
            label = "Avg Score",
            value = "${uiState.averageScore}pts",
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Small glass card displaying one stat value above its label with a leading emoji.
 *
 * @param emoji Decorative emoji shown above the value.
 * @param label Descriptor shown below the value.
 * @param value Formatted stat string.
 */
@Composable
private fun MiniStatCard(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(STAT_CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

// ── Accuracy by region ────────────────────────────────────────────────────────

/**
 * Glass card containing one horizontal gradient bar per region, showing that
 * region's accuracy percentage for the last 30 days.
 *
 * Regions with no recorded answers display a 0 % bar.
 *
 * @param regionAccuracies Map of region name → accuracy fraction (0.0..1.0).
 */
@Composable
private fun RegionAccuracyCard(regionAccuracies: Map<String, Float>) {
    StatsGlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            REGION_BARS.forEach { config ->
                val accuracy = regionAccuracies[config.name] ?: 0f
                RegionBarRow(config = config, accuracyFraction = accuracy)
            }
        }
    }
}

/**
 * Single row in the region accuracy card: emoji + name on the left, fill bar
 * in the centre, and percentage label on the right.
 *
 * @param config Visual configuration for this region (emoji, name, colours).
 * @param accuracyFraction Accuracy in the range 0.0..1.0.
 */
@Composable
private fun RegionBarRow(
    config: RegionBarConfig,
    accuracyFraction: Float,
) {
    val clampedFraction = accuracyFraction.coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = config.emoji, fontSize = 16.sp)
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = "${(clampedFraction * PERCENT_INT).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = config.primary,
                fontWeight = FontWeight.Bold,
            )
        }

        // Progress track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BAR_HEIGHT)
                .clip(RoundedCornerShape(BAR_RADIUS))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            // Gradient fill
            if (clampedFraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = clampedFraction)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(config.gradientStart, config.primary),
                            ),
                        ),
                )
            }
        }
    }
}

// ── Recent quizzes ────────────────────────────────────────────────────────────

/**
 * Card row showing a summary of a single recent quiz session.
 *
 * Displays the formatted completion date, a coloured region pill, the
 * correct/total score, and the accuracy percentage.
 *
 * @param session The session data to display.
 */
@Composable
private fun RecentQuizRow(session: RecentQuizSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(STAT_CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Date
            Text(
                text = session.completedAt.formatSessionDate(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(56.dp),
            )

            // Region pill
            RegionPill(region = session.region, gameMode = session.gameMode)

            // Score
            Text(
                text = "${session.correctAnswers}/${session.totalQuestions}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )

            // Accuracy
            Text(
                text = "${session.accuracyPercent}%",
                style = MaterialTheme.typography.bodyMedium,
                color = regionTagColor(session.region),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Coloured pill label showing the region (or game-mode fallback when region
 * is `null` — e.g. "Streak" or "Daily").
 *
 * @param region Session region name, or `null` for streak/speed modes.
 * @param gameMode Used to derive the label when [region] is null.
 */
@Composable
private fun RegionPill(region: String?, gameMode: String) {
    val label = pillLabel(region, gameMode)
    val color = regionTagColor(region)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = PILL_ALPHA))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

/**
 * Reusable glassmorphism card container used throughout the Statistics screen.
 *
 * @param content Composable content placed inside the card.
 */
@Composable
private fun StatsGlassCard(content: @Composable () -> Unit) {
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
 * Bold section header label.
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

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Returns the accent [Color] for a given region name, used for both the
 * recent-quiz row accuracy text and the region pill background.
 *
 * @param region Region name as stored in the database (e.g. `"Asia"`), or
 *   `null` for mixed/streak sessions.
 */
private fun regionTagColor(region: String?): Color = when (region?.lowercase()) {
    "asia" -> RegionAsiaPrimary
    "europe" -> RegionEuropePrimary
    "africa" -> RegionAfricaPrimary
    "americas" -> RegionAmericasPrimary
    "oceania" -> RegionOceaniaPrimary
    else -> TextBody
}

/**
 * Returns the pill label for a recent quiz row.
 *
 * Uses the [region] when non-null; falls back to a human-readable [gameMode]
 * label for modes that have no fixed region (streak, speed, daily).
 *
 * @param region Session region name, or `null`.
 * @param gameMode One of `"classic"`, `"daily"`, `"streak"`, `"speed"`.
 */
private fun pillLabel(region: String?, gameMode: String): String =
    when {
        !region.isNullOrBlank() && region != "mixed" -> region
        gameMode == "daily" -> "Daily"
        gameMode == "streak" -> "Streak"
        gameMode == "speed" -> "Speed"
        else -> "Mixed"
    }

/**
 * Formats a Unix timestamp (ms) as a short date string relative to today.
 *
 * | Offset   | Result      |
 * |----------|-------------|
 * | Today    | `"Today"`   |
 * | Yesterday| `"Yesterday"`|
 * | Other    | `"Apr 12"`  |
 *
 * @return Human-readable date label.
 */
private fun Long.formatSessionDate(): String {
    val sessionDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
    val today = LocalDate.now(ZoneOffset.UTC)
    return when (sessionDate) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> sessionDate.format(MONTH_DAY_FORMATTER)
    }
}

/**
 * Formats a total play time in milliseconds as a compact human-readable string.
 *
 * | Duration        | Result    |
 * |-----------------|-----------|
 * | Under 1 minute  | `"<1m"`   |
 * | Under 60 minutes| `"34m"`   |
 * | 1 hour or more  | `"2h 15m"`|
 *
 * @return Compact duration string.
 */
private fun Long.formatTotalTime(): String {
    val totalSeconds = this / MILLIS_PER_SECOND
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val hours = minutes / MINUTES_PER_HOUR
    val remainingMinutes = minutes % MINUTES_PER_HOUR
    return when {
        minutes == 0L -> "<1m"
        hours == 0L -> "${minutes}m"
        remainingMinutes == 0L -> "${hours}h"
        else -> "${hours}h ${remainingMinutes}m"
    }
}

private const val PERCENT_INT = 100
private const val PILL_ALPHA = 0.15f
private const val MILLIS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val MINUTES_PER_HOUR = 60L
