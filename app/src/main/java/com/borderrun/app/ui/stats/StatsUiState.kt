package com.borderrun.app.ui.stats

/**
 * A single entry in the "Recent Quizzes" list on the Statistics screen.
 *
 * Derived from [com.borderrun.app.data.local.entity.QuizSessionEntity] in the
 * ViewModel and contains only the fields needed for display.
 *
 * @property sessionId Primary key of the session.
 * @property completedAt Unix timestamp (ms) when the session finished.
 * @property region The region the quiz was played in, or `null` for streak/speed modes.
 * @property gameMode One of `"classic"`, `"daily"`, `"streak"`, `"speed"`.
 * @property score Total points earned.
 * @property correctAnswers Number of questions answered correctly.
 * @property totalQuestions Total questions presented.
 */
data class RecentQuizSession(
    val sessionId: Int,
    val completedAt: Long,
    val region: String?,
    val gameMode: String,
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
) {
    /** Accuracy as an integer percentage (0–100). */
    val accuracyPercent: Int
        get() = if (totalQuestions == 0) 0 else (correctAnswers * 100) / totalQuestions
}

/**
 * UI state for the Statistics screen, modelled as a flat data class.
 *
 * [StatsViewModel] emits this via [StatsViewModel.uiState]. The initial value
 * has [isLoading] `= true`; the first combined Room emission flips it to `false`.
 *
 * @property isLoading `true` while the database flows have not yet emitted.
 * @property totalQuizzes Number of completed quiz sessions across all modes.
 * @property questionsAnswered Cumulative questions answered across all sessions.
 * @property overallAccuracyPercent Overall accuracy as an integer percentage (0–100).
 * @property currentStreak Count of distinct active days in the last 30 days.
 * @property totalTimePlayedMs Sum of [RecentQuizSession.completedAt] durations in ms.
 * @property averageScore Mean score per quiz session (integer, rounded down).
 * @property regionAccuracies Map of region name → accuracy fraction (0.0..1.0).
 *   Only regions with at least one recorded answer appear in the map.
 * @property recentSessions Most recent [RECENT_SESSIONS_LIMIT] quiz sessions,
 *   newest first.
 */
data class StatsUiState(
    val isLoading: Boolean = true,
    val totalQuizzes: Int = 0,
    val questionsAnswered: Int = 0,
    val overallAccuracyPercent: Int = 0,
    val currentStreak: Int = 0,
    val totalTimePlayedMs: Long = 0L,
    val averageScore: Int = 0,
    val regionAccuracies: Map<String, Float> = emptyMap(),
    val recentSessions: List<RecentQuizSession> = emptyList(),
)
