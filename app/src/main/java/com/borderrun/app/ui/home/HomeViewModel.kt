package com.borderrun.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borderrun.app.domain.usecase.GetDailyContentUseCase
import com.borderrun.app.domain.usecase.GetHomeStatsUseCase
import com.borderrun.app.domain.usecase.GetRegionCountsUseCase
import com.borderrun.app.domain.usecase.GetWeaknessDataUseCase
import com.borderrun.app.domain.usecase.GetWeeklyActivityUseCase
import com.borderrun.app.domain.usecase.SyncCountriesUseCase
import com.borderrun.app.domain.usecase.SyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 *
 * Combines five use-case flows into a single [uiState] [StateFlow] using
 * [combine], then chains a second [combine] with [_syncState] to derive
 * [HomeUiState.isLoading] and [HomeUiState.syncError].
 *
 * On creation, [init] launches [syncCountriesUseCase] so country data is
 * refreshed on every cold start (skipped automatically if cache is fresh).
 *
 * @property getHomeStatsUseCase Aggregates streak, total answered, accuracy,
 *   and high score.
 * @property getDailyContentUseCase Returns today's challenge and mystery teaser.
 * @property getWeaknessDataUseCase Returns weakness data when ≥ 20 answers exist.
 * @property getWeeklyActivityUseCase Returns the Mon–Sun activity map.
 * @property getRegionCountsUseCase Returns per-region country counts.
 * @property syncCountriesUseCase Fetches fresh country data; skipped when cache
 *   is younger than 24 hours.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeStatsUseCase: GetHomeStatsUseCase,
    private val getDailyContentUseCase: GetDailyContentUseCase,
    private val getWeaknessDataUseCase: GetWeaknessDataUseCase,
    private val getWeeklyActivityUseCase: GetWeeklyActivityUseCase,
    private val getRegionCountsUseCase: GetRegionCountsUseCase,
    private val syncCountriesUseCase: SyncCountriesUseCase,
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

    /** Tracks the in-flight / completed / errored state of the initial sync. */
    private enum class SyncState { Syncing, Done, Error }

    private val _syncState = MutableStateFlow(SyncState.Syncing)

    /**
     * Unified Home screen state.
     *
     * The 5-flow [combine] produces the base data payload. A second [combine]
     * with [_syncState] derives:
     * - [HomeUiState.isLoading] — `true` while the cache is empty **and** sync
     *   hasn't finished (avoids showing an empty region grid on cold start).
     * - [HomeUiState.syncError] — non-null only when sync failed **and** no
     *   cached data exists; `null` when stale cache covers the failure.
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
    }.combine(_syncState) { state, syncState ->
        val hasCachedData = state.regionCounts.values.sum() > 0
        state.copy(
            isLoading = !hasCachedData && syncState == SyncState.Syncing,
            syncError = if (syncState == SyncState.Error && !hasCachedData) {
                "Couldn't load country data. Please check your internet connection."
            } else {
                null
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = HomeUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            val result = syncCountriesUseCase()
            _syncState.value = when (result) {
                is SyncResult.Failed -> SyncState.Error
                else -> SyncState.Done
            }
        }
    }

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
