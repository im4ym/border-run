package com.borderrun.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borderrun.app.domain.model.DAILY_CHALLENGE_QUESTION_COUNT
import com.borderrun.app.domain.model.DAILY_CHALLENGE_TIME_ESTIMATE_MINUTES
import com.borderrun.app.domain.model.DailyContent
import com.borderrun.app.domain.model.WeaknessData
import com.borderrun.app.ui.components.BorderRunBottomNav
import com.borderrun.app.ui.components.BottomNavTab
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
import com.borderrun.app.ui.theme.RegionAfricaGradientStart
import com.borderrun.app.ui.theme.RegionAfricaPrimary
import com.borderrun.app.ui.theme.RegionAmericasGradientStart
import com.borderrun.app.ui.theme.RegionAmericasPrimary
import com.borderrun.app.ui.theme.RegionAsiaGradientStart
import com.borderrun.app.ui.theme.RegionAsiaPrimary
import com.borderrun.app.ui.theme.RegionEuropeGradientStart
import com.borderrun.app.ui.theme.RegionEuropePrimary
import com.borderrun.app.ui.theme.SuccessGreen
import com.borderrun.app.ui.theme.TextHeading
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

// ── Layout constants ──────────────────────────────────────────────────────────

private val CARD_HORIZONTAL_PADDING = 20.dp
private val CARD_VERTICAL_PADDING = 20.dp
private val SECTION_SPACING = 16.dp
private val ICON_BOX_SIZE = 44.dp
private val ICON_BOX_RADIUS = 12.dp

// ── Region display data ───────────────────────────────────────────────────────

/**
 * Static display metadata for one of the four Home screen region cards.
 *
 * @property name Region name matching the RestCountries API value.
 * @property emoji Emoji glyph shown inside the gradient icon box.
 * @property gradientStart Lighter gradient stop colour from [com.borderrun.app.ui.theme].
 * @property gradientEnd Darker gradient stop colour from [com.borderrun.app.ui.theme].
 */
private data class RegionDisplayInfo(
    val name: String,
    val emoji: String,
    val gradientStart: Color,
    val gradientEnd: Color,
)

private val HOME_REGIONS = listOf(
    RegionDisplayInfo("Asia", "🌏", RegionAsiaGradientStart, RegionAsiaPrimary),
    RegionDisplayInfo("Europe", "🌍", RegionEuropeGradientStart, RegionEuropePrimary),
    RegionDisplayInfo("Africa", "☀️", RegionAfricaGradientStart, RegionAfricaPrimary),
    RegionDisplayInfo("Americas", "🌎", RegionAmericasGradientStart, RegionAmericasPrimary),
)

// ── Root screen composable ────────────────────────────────────────────────────

/**
 * Root composable for the Landing / Home screen.
 *
 * Collects [HomeViewModel.uiState] and renders the full scrollable layout
 * inside a gradient-background [Scaffold]. Navigation callbacks are passed in
 * from [com.borderrun.app.navigation.BorderRunNavGraph] so the composable
 * remains independently testable.
 *
 * @param onRegionClick Invoked with the region name when a region card is tapped.
 * @param onDailyChallengeClick Invoked when the "Start Challenge" button is tapped.
 * @param onMysteryClick Invoked when the Mystery Country play button is tapped.
 * @param onWeaknessClick Invoked with the region name when the Weakness Trainer
 *   "Practice" button is tapped.
 * @param onQuizClick Invoked when the Quiz tab in the bottom nav is tapped.
 * @param onStatsClick Invoked when the Stats tab in the bottom nav is tapped.
 * @param onSettingsClick Invoked when the Settings tab is tapped.
 * @param viewModel Hilt-injected [HomeViewModel]. Defaults to the Hilt-provided
 *   instance for the current nav back-stack entry.
 */
@Composable
fun HomeScreen(
    onRegionClick: (String) -> Unit,
    onDailyChallengeClick: () -> Unit,
    onMysteryClick: () -> Unit,
    onWeaknessClick: (String) -> Unit,
    onQuizClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onExplorerClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val isDark = LocalIsDarkTheme.current
    val gradientBackground = Brush.verticalGradient(
        colors = if (isDark) listOf(DarkGradientStop1, DarkGradientStop2, DarkGradientStop3, DarkGradientStop4)
                 else listOf(GradientMint, GradientTeal, GradientCyan, GradientSky),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BorderRunBottomNav(
                    currentTab = BottomNavTab.Home,
                    onTabSelected = { tab ->
                        when (tab) {
                            BottomNavTab.Home -> Unit
                            BottomNavTab.Quiz -> onQuizClick()
                            BottomNavTab.Stats -> onStatsClick()
                            BottomNavTab.Settings -> onSettingsClick()
                            BottomNavTab.Explorer -> onExplorerClick()
                        }
                    },
                )
            },
        ) { innerPadding ->
            val syncError = uiState.syncError
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else if (syncError != null) {
                SyncErrorBanner(
                    message = syncError,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 20.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
                ) {
                    item {
                        GreetingSection(
                            greeting = uiState.greeting,
                            streak = uiState.streak,
                        )
                    }
                    item {
                        DailyChallengeCard(
                            dailyContent = uiState.dailyContent,
                            onStartClick = onDailyChallengeClick,
                        )
                    }
                    item {
                        MysteryCard(
                            dailyContent = uiState.dailyContent,
                            onPlayClick = onMysteryClick,
                        )
                    }
                    uiState.weaknessData?.let { weakness ->
                        item {
                            WeaknessTrainerCard(
                                weaknessData = weakness,
                                onPracticeClick = { onWeaknessClick(weakness.regionName) },
                            )
                        }
                    }
                    item {
                        RegionGridSection(
                            regionCounts = uiState.regionCounts,
                            onRegionClick = onRegionClick,
                        )
                    }
                    item {
                        WeeklyProgressSection(weeklyActivity = uiState.weeklyActivity)
                    }
                    item {
                        StatsSummaryCard(
                            totalAnswered = uiState.totalAnswered,
                            accuracy = uiState.accuracy,
                            streak = uiState.streak,
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

// ── Sync error banner ─────────────────────────────────────────────────────────

/**
 * Full-area error state shown when the initial sync failed and the cache is
 * empty, so there is nothing to display.
 *
 * @param message Human-readable error description from [HomeUiState.syncError].
 * @param modifier Modifier applied to the outer [Box].
 */
@Composable
private fun SyncErrorBanner(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Text(
                text = "⚠️",
                fontSize = 48.sp,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = ErrorRed,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

// ── Section: Greeting + Streak ────────────────────────────────────────────────

/**
 * Top header section with a time-of-day greeting on the left and the streak
 * pill on the right (only shown when [streak] > 0).
 *
 * @param greeting Time-of-day greeting string from [computeGreeting].
 * @param streak Current day-streak count.
 */
@Composable
private fun GreetingSection(greeting: String, streak: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Ready to explore?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (streak > 0) {
            StreakPill(streak = streak)
        }
    }
}

/**
 * Gradient pill badge displaying the current day streak with a flame emoji.
 *
 * @param streak The streak count to display.
 */
@Composable
private fun StreakPill(streak: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(listOf(CtaGradientStart, CtaGradientEnd)),
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = "🔥", fontSize = 14.sp)
            Text(
                text = "$streak day streak",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            )
        }
    }
}

// ── Section: Daily Challenge ──────────────────────────────────────────────────

/**
 * Glass-morphism card for the Daily Challenge.
 *
 * Shows the challenge title, description, question count, time estimate, and a
 * gradient CTA button. Adapts when no challenge exists yet (WorkManager has not
 * yet run) or when the challenge has already been completed today.
 *
 * @param dailyContent Today's [DailyContent] from the ViewModel state.
 * @param onStartClick Invoked when the CTA button is tapped (only when enabled).
 */
@Composable
private fun DailyChallengeCard(
    dailyContent: DailyContent,
    onStartClick: () -> Unit,
) {
    val challenge = dailyContent.challenge
    GlassCard {
        Column(
            modifier = Modifier.padding(
                horizontal = CARD_HORIZONTAL_PADDING,
                vertical = CARD_VERTICAL_PADDING,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GradientIconBox(
                    emoji = "📅",
                    gradientStart = CtaGradientStart,
                    gradientEnd = CtaGradientEnd,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Challenge",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = challenge?.title ?: "Preparing today's challenge…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (challenge?.completed == true) {
                    StatusBadge(text = "✓ Done", background = SuccessGreen.copy(alpha = 0.15f), textColor = SuccessGreen)
                } else {
                    StatusBadge(text = "2×", background = PrimaryGreen.copy(alpha = 0.15f), textColor = PrimaryGreen)
                }
            }

            if (challenge != null) {
                Text(
                    text = "$DAILY_CHALLENGE_QUESTION_COUNT questions  •  ~$DAILY_CHALLENGE_TIME_ESTIMATE_MINUTES min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            GradientButton(
                text = when {
                    challenge == null -> "Start Daily Quiz"
                    challenge.completed -> "View Results"
                    else -> "Start Challenge"
                },
                enabled = true,
                onClick = onStartClick,
            )
        }
    }
}

// ── Section: Mystery Country ──────────────────────────────────────────────────

/**
 * Glass-morphism card for the Daily Mystery Country feature.
 *
 * Displays a teaser row with the mystery state (unsolved / solved) and a compact
 * "Play" or "Solved" action button.
 *
 * @param dailyContent Today's [DailyContent] from the ViewModel state.
 * @param onPlayClick Invoked when the user taps the play button.
 */
@Composable
private fun MysteryCard(dailyContent: DailyContent, onPlayClick: () -> Unit) {
    val mystery = dailyContent.mystery
    GlassCard {
        Row(
            modifier = Modifier.padding(
                horizontal = CARD_HORIZONTAL_PADDING,
                vertical = CARD_VERTICAL_PADDING,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GradientIconBox(
                emoji = "🌍",
                gradientStart = RegionEuropeGradientStart,
                gradientEnd = RegionEuropePrimary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mystery Country",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = when {
                        mystery == null -> "Preparing today's mystery…"
                        mystery.solved -> "Solved in ${mystery.attempts + 1} guess${if (mystery.attempts + 1 == 1) "" else "es"}  ✓"
                        mystery.cluesRevealed > 0 -> "${mystery.cluesRevealed} clue${if (mystery.cluesRevealed == 1) "" else "s"} revealed — keep going!"
                        else -> "Can you guess today's country?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            val isSolved = mystery?.solved == true
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            if (isSolved) listOf(SuccessGreen, SuccessGreen)
                            else listOf(RegionEuropeGradientStart, RegionEuropePrimary),
                        ),
                    )
                    .then(
                        if (!isSolved) Modifier.clickable { onPlayClick() }
                        else Modifier,
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = if (isSolved) "Solved" else "Play",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

// ── Section: Weakness Trainer ─────────────────────────────────────────────────

/**
 * Contextual card shown only when the user has ≥ 20 answers and a weakest
 * region can be identified.
 *
 * Displays the region name, current accuracy percentage, and a CTA to start a
 * targeted practice quiz.
 *
 * @param weaknessData Non-null weakness information from [HomeUiState.weaknessData].
 * @param onPracticeClick Invoked when the "Practice" CTA is tapped.
 */
@Composable
private fun WeaknessTrainerCard(weaknessData: WeaknessData, onPracticeClick: () -> Unit) {
    val accuracyPct = (weaknessData.accuracyFraction * 100).roundToInt()
    val amberGradientStart = Color(0xFFFBBF24)
    val amberGradientEnd = Color(0xFFF59E0B)

    GlassCard {
        Column(
            modifier = Modifier.padding(
                horizontal = CARD_HORIZONTAL_PADDING,
                vertical = CARD_VERTICAL_PADDING,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GradientIconBox(
                    emoji = "⚡",
                    gradientStart = amberGradientStart,
                    gradientEnd = amberGradientEnd,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Weakness Trainer",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(amberGradientStart.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "Recommended",
                                fontSize = 10.sp,
                                color = amberGradientEnd,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Text(
                        text = "You score lower in ${weaknessData.regionName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = amberGradientEnd,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Current accuracy in ${weaknessData.regionName}: $accuracyPct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GradientButton(
                text = "Practice ${weaknessData.regionName}",
                onClick = onPracticeClick,
            )
        }
    }
}

// ── Section: Region Grid ──────────────────────────────────────────────────────

/**
 * 2 × 2 grid of region cards for Asia, Europe, Africa, and Americas.
 *
 * Each card shows the region's gradient icon, name, and cached country count.
 * Tapping a card invokes [onRegionClick] with the region name.
 *
 * @param regionCounts Map of region name → country count from Room.
 *   Empty entries default to 0 before the first API sync.
 * @param onRegionClick Invoked with the region name when a card is tapped.
 */
@Composable
private fun RegionGridSection(
    regionCounts: Map<String, Int>,
    onRegionClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Explore by Region")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HOME_REGIONS.take(2).forEach { info ->
                RegionCard(
                    info = info,
                    count = regionCounts[info.name] ?: 0,
                    modifier = Modifier.weight(1f),
                    onClick = { onRegionClick(info.name) },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HOME_REGIONS.drop(2).forEach { info ->
                RegionCard(
                    info = info,
                    count = regionCounts[info.name] ?: 0,
                    modifier = Modifier.weight(1f),
                    onClick = { onRegionClick(info.name) },
                )
            }
        }
    }
}

/**
 * Single region card with a gradient icon box, region name, and country count.
 *
 * Uses a 16 dp corner radius and the glass-morphism card style.
 *
 * @param info Static display metadata for the region.
 * @param count Number of countries cached for this region.
 * @param modifier Modifier applied to the [Card].
 * @param onClick Invoked when the card is tapped.
 */
@Composable
private fun RegionCard(
    info: RegionDisplayInfo,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(info.gradientStart, info.gradientEnd),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = info.emoji, fontSize = 18.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = info.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$count countries",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Section: Weekly Progress ──────────────────────────────────────────────────

/**
 * Glassmorphism card containing a Mon–Fri row of face icons coloured by daily
 * practice activity.
 *
 * Face colours:
 * - **Green 😊** — answers recorded on that day.
 * - **Red 😔** — past day with no activity.
 * - **Primary 🙂** — today with no activity yet.
 * - **Gray circle** — future day (no data possible).
 *
 * @param weeklyActivity Map of `"YYYY-MM-DD"` → answer count for the current week.
 */
@Composable
private fun WeeklyProgressSection(weeklyActivity: Map<String, Int>) {
    val today = remember { LocalDate.now(ZoneOffset.UTC) }
    val monday = remember(today) { today.with(DayOfWeek.MONDAY) }
    val weekDayLabels = listOf("M", "T", "W", "T", "F")

    // Pairs of (LocalDate, "YYYY-MM-DD" dateString) for Mon–Fri
    val weekDays = remember(monday) {
        (0..4).map { offset ->
            val date = monday.plusDays(offset.toLong())
            Pair(date, date.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
    }

    GlassCard {
        Column(
            modifier = Modifier.padding(
                horizontal = CARD_HORIZONTAL_PADDING,
                vertical = CARD_VERTICAL_PADDING,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionHeader(title = "This Week")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                weekDays.forEachIndexed { index, (date, dateStr) ->
                    DayFaceItem(
                        dayLabel = weekDayLabels[index],
                        hasActivity = weeklyActivity.containsKey(dateStr),
                        isFuture = date.isAfter(today),
                        isToday = date == today,
                    )
                }
            }
        }
    }
}

/**
 * A single day's face icon for the Weekly Progress row.
 *
 * @param dayLabel Short day abbreviation: `"M"`, `"T"`, `"W"`, etc.
 * @param hasActivity Whether at least one answer was recorded on this day.
 * @param isFuture Whether the date is in the future (no data expected).
 * @param isToday Whether this represents today's date.
 */
@Composable
private fun DayFaceItem(
    dayLabel: String,
    hasActivity: Boolean,
    isFuture: Boolean,
    isToday: Boolean,
) {
    val bgColor = when {
        isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
        hasActivity -> SuccessGreen.copy(alpha = 0.18f)
        isToday -> PrimaryGreen.copy(alpha = 0.12f)
        else -> ErrorRed.copy(alpha = 0.12f)
    }
    val labelColor = when {
        isFuture -> MaterialTheme.colorScheme.onSurfaceVariant
        hasActivity -> SuccessGreen
        isToday -> PrimaryGreen
        else -> ErrorRed
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center,
        ) {
            if (!isFuture) {
                val face = if (hasActivity) "😊" else if (isToday) "🙂" else "😔"
                Text(text = face, fontSize = 20.sp)
            }
        }
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            fontWeight = if (hasActivity && !isFuture) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

// ── Section: Stats Summary ────────────────────────────────────────────────────

/**
 * Glassmorphism card with three stats in a horizontal row: total questions
 * answered, overall accuracy percentage, and current day streak.
 *
 * @param totalAnswered Cumulative questions answered across all sessions.
 * @param accuracy Overall accuracy fraction in `0.0..1.0`.
 * @param streak Current day-streak count.
 */
@Composable
private fun StatsSummaryCard(
    totalAnswered: Int,
    accuracy: Float,
    streak: Int,
) {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CARD_HORIZONTAL_PADDING, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatItem(value = "$totalAnswered", label = "Answered")
            StatDivider()
            StatItem(value = "${(accuracy * 100).roundToInt()}%", label = "Accuracy")
            StatDivider()
            StatItem(value = "$streak", label = "Day Streak")
        }
    }
}

/**
 * A single labelled stat value used in [StatsSummaryCard].
 *
 * @param value The formatted stat value to display (e.g. `"120"`, `"75%"`).
 * @param label Short description below the value (e.g. `"Answered"`).
 */
@Composable
private fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Thin vertical divider line used between stat items in [StatsSummaryCard]. */
@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
    )
}

// ── Reusable sub-components ───────────────────────────────────────────────────

/**
 * Glass-morphism card container matching the Border Run design system.
 *
 * Uses a 22 dp corner radius, [CardSurface] background (55 % opacity white),
 * and a 0.5 dp [MaterialTheme.colorScheme.outline] stroke. Zero elevation keeps the card from casting
 * shadows that would conflict with the gradient background.
 *
 * @param modifier Optional [Modifier] for the card root.
 * @param content The content to render inside the card.
 */
@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        content()
    }
}

/**
 * Gradient CTA button matching the Border Run design system.
 *
 * Renders a full-width [Box] with a horizontal gradient fill and rounded 16 dp
 * corners. When [enabled] is `false`, the gradient is replaced with a muted
 * grey and the click handler is suppressed.
 *
 * @param text Label displayed inside the button.
 * @param onClick Invoked when the button is tapped (only when [enabled]).
 * @param enabled Whether the button accepts tap events. Defaults to `true`.
 * @param modifier Optional [Modifier] for the outer [Box].
 */
@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val gradientBrush = if (enabled) {
        Brush.horizontalGradient(listOf(CtaGradientStart, CtaGradientEnd))
    } else {
        Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradientBrush)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/**
 * Squared icon box with a linear gradient background used in card headers.
 *
 * Size is [ICON_BOX_SIZE] × [ICON_BOX_SIZE] with [ICON_BOX_RADIUS] corners.
 *
 * @param emoji The emoji glyph to display inside the box.
 * @param gradientStart Gradient start (top-left) colour.
 * @param gradientEnd Gradient end (bottom-right) colour.
 */
@Composable
private fun GradientIconBox(
    emoji: String,
    gradientStart: Color,
    gradientEnd: Color,
) {
    Box(
        modifier = Modifier
            .size(ICON_BOX_SIZE)
            .clip(RoundedCornerShape(ICON_BOX_RADIUS))
            .background(
                Brush.linearGradient(listOf(gradientStart, gradientEnd)),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = 20.sp)
    }
}

/**
 * Small rounded badge used for status labels on cards (e.g. `"✓ Done"`, `"2×"`).
 *
 * @param text The badge text.
 * @param background Background tint colour.
 * @param textColor Text/foreground colour.
 */
@Composable
private fun StatusBadge(text: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * Bold section header text styled with [MaterialTheme.typography.titleLarge]
 * and [TextHeading] colour.
 *
 * @param title The header text.
 * @param modifier Optional [Modifier].
 */
@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}
