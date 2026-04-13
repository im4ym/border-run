package com.borderrun.app.ui.home

import com.borderrun.app.domain.model.DailyContent
import com.borderrun.app.domain.model.WeaknessData

/**
 * UI state for the Home screen, modelled as a flat data class with an
 * [isLoading] flag.
 *
 * [HomeViewModel] emits this via [HomeViewModel.uiState]. The initial value
 * has `isLoading = true`; the first Room emission flips it to `false`.
 *
 * @property isLoading `true` while the combined flow has not yet emitted its
 *   first value. Shows a loading indicator in the UI.
 * @property greeting Time-of-day greeting string: `"Good Morning"`,
 *   `"Good Afternoon"`, or `"Good Evening"`.
 * @property streak Number of consecutive active days in the last 30 days.
 * @property dailyContent Today's challenge and mystery teaser. Either field
 *   may be `null` if [com.borderrun.app.worker.ContentSyncWorker] has not
 *   yet run today.
 * @property weeklyActivity Map of `"YYYY-MM-DD"` → answer count for the
 *   current week (Mon–Sun). Missing dates had zero activity.
 * @property totalAnswered Cumulative questions answered across all sessions.
 * @property accuracy Overall accuracy fraction in `0.0..1.0`.
 * @property weaknessData Populated when ≥ 20 answers exist and a weakest
 *   region can be identified; `null` otherwise.
 * @property regionCounts Map of region name → cached country count.
 *   Empty before the first API sync.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val greeting: String = "",
    val streak: Int = 0,
    val dailyContent: DailyContent = DailyContent(challenge = null, mystery = null),
    val weeklyActivity: Map<String, Int> = emptyMap(),
    val totalAnswered: Int = 0,
    val accuracy: Float = 0f,
    val weaknessData: WeaknessData? = null,
    val regionCounts: Map<String, Int> = emptyMap(),
    /**
     * Non-null when the sync failed **and** the cache is empty, so the UI
     * can show a full-screen error banner. `null` when data is loading,
     * loaded successfully, or the API failed but stale cache exists (silent
     * failure — the user still sees data).
     */
    val syncError: String? = null,
)
