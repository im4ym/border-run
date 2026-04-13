package com.borderrun.app.domain.model

/**
 * Aggregated statistics shown on the Home screen's summary row and used by
 * the streak pill.
 *
 * @property streak Number of consecutive active days (any session completed) in
 *   the last 30 days, as calculated by [com.borderrun.app.domain.usecase.GetHomeStatsUseCase].
 * @property totalAnswered Total questions answered across all quiz sessions.
 * @property accuracy Overall answer accuracy as a fraction in `0.0..1.0`.
 *   Defaults to `0f` when no answers have been recorded yet.
 * @property streakHighScore Highest score ever achieved in Streak game mode.
 */
data class HomeStats(
    val streak: Int = 0,
    val totalAnswered: Int = 0,
    val accuracy: Float = 0f,
    val streakHighScore: Int = 0,
)
