package com.borderrun.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borderrun.app.domain.usecase.GetDailyContentUseCase
import com.borderrun.app.domain.usecase.GetHomeStatsUseCase
import com.borderrun.app.domain.usecase.GetRegionCountsUseCase
import com.borderrun.app.domain.usecase.GetWeaknessDataUseCase
import com.borderrun.app.domain.usecase.GetWeeklyActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 *
 * Combines five use-case flows into a single [uiState] [StateFlow] using
 * [combine]. All data comes from Room (offline-first), so the initial emission
 * is near-instantaneous.
 *
 * Timestamps are computed once per ViewModel lifetime (lazy). They are stable
 * for the duration of a single user session; the ViewModel is recreated on
 * activity recreation, which resets the timestamps to the current time.
 *
 * @property getHomeStatsUseCase Aggregates streak, total answered, accuracy,
 *   and high score.
 * @property getDailyContentUseCase Returns today's challenge and mystery teaser.
 * @property getWeaknessDataUseCase Returns weakness data when ≥ 20 answers exist.
 * @property getWeeklyActivityUseCase Returns the Mon–Sun activity map.
 * @property getRegionCountsUseCase Returns per-region country counts.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeStatsUseCase: GetHomeStatsUseCase,
    private val getDailyContentUseCase: GetDailyContentUseCase,
    private val getWeaknessDataUseCase: GetWeaknessDataUseCase,
    private val getWeeklyActivityUseCase: GetWeeklyActivityUseCase,
    private val getRegionCountsUseCase: GetRegionCountsUseCase,
) : ViewModel() {

    /** Today at midnight UTC, used to look up today's daily content. */
    private val todayMs: Long by lazy {
        LocalDate.now(ZoneOffset.UTC)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }

    /**
     * Monday midnight UTC for the current week.
     *
     * If today is Monday the value equals [todayMs].
     */
    private val weekStartMs: Long by lazy {
        val today = LocalDate.now(ZoneOffset.UTC)
        today.with(DayOfWeek.MONDAY)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }

    /**
     * Unified Home screen state.
     *
     * Uses [SharingStarted.WhileSubscribed] with a 5-second timeout so the
     * upstream Room flows pause when the screen is backgrounded and resume on
     * return without emitting stale data.
     *
     * The [HomeUiState.isLoading] flag is `true` until the first combined
     * emission arrives.
     */
    val uiState: StateFlow<HomeUiState> = combine(
        getHomeStatsUseCase(),
        getDailyContentUseCase(todayMs),
        getWeaknessDataUseCase(),
        getWeeklyActivityUseCase(weekStartMs),
        getRegionCountsUseCase(),
    ) { stats, dailyContent, weaknessData, weeklyActivity, regionCounts ->
        HomeUiState(
            isLoading = false,
            greeting = computeGreeting(),
            streak = stats.streak,
            dailyContent = dailyContent,
            weeklyActivity = weeklyActivity,
            totalAnswered = stats.totalAnswered,
            accuracy = stats.accuracy,
            weaknessData = weaknessData,
            regionCounts = regionCounts,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = HomeUiState(isLoading = true),
    )

    companion object {
        /** Delay in ms before the upstream flows are cancelled after the last subscriber leaves. */
        private const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}

/**
 * Returns a time-of-day greeting based on the device's local clock.
 *
 * | Hour range | Greeting         |
 * |------------|------------------|
 * | 00–11      | Good Morning     |
 * | 12–16      | Good Afternoon   |
 * | 17–23      | Good Evening     |
 *
 * @return One of `"Good Morning"`, `"Good Afternoon"`, or `"Good Evening"`.
 */
internal fun computeGreeting(): String =
    when (LocalTime.now().hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
