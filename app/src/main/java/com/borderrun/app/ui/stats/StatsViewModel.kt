package com.borderrun.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borderrun.app.data.local.dao.QuizSessionDao
import com.borderrun.app.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * ViewModel for the Statistics screen.
 *
 * Combines flows from [StatsRepository] and [QuizSessionDao] into a single
 * [uiState] [StateFlow]. Per-session aggregates (total time, average score,
 * recent history) are derived from the raw session list emitted by the DAO;
 * accuracy and streak data are routed through the repository.
 *
 * @property statsRepository Provides overall accuracy, streak, and per-region
 *   accuracy maps.
 * @property quizSessionDao Direct DAO access for the full session list — used
 *   to derive total quizzes, total time played, average score, and recent
 *   quiz history.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val quizSessionDao: QuizSessionDao,
) : ViewModel() {

    /** Lower bound timestamp for 30-day rolling windows. */
    private val thirtyDaysAgoMs: Long =
        Instant.now().minus(STREAK_WINDOW_DAYS, ChronoUnit.DAYS).toEpochMilli()

    /**
     * Unified Statistics screen state.
     *
     * Merges five flows — [StatsRepository.getTotalAnswered],
     * [StatsRepository.getOverallAccuracy], [StatsRepository.getStreak],
     * [StatsRepository.getAccuracyByRegion], and
     * [QuizSessionDao.getAllSessions] — into a single [StatsUiState] emission.
     *
     * From the raw session list the transform computes:
     * - [StatsUiState.totalQuizzes] — list size
     * - [StatsUiState.totalTimePlayedMs] — sum of [durationMs]
     * - [StatsUiState.averageScore] — mean score per session
     * - [StatsUiState.recentSessions] — first [RECENT_SESSIONS_LIMIT] entries
     */
    val uiState: StateFlow<StatsUiState> = combine(
        statsRepository.getTotalAnswered(),
        statsRepository.getOverallAccuracy(),
        statsRepository.getStreak(thirtyDaysAgoMs),
        statsRepository.getAccuracyByRegion(thirtyDaysAgoMs),
        quizSessionDao.getAllSessions(),
    ) { totalAnswered, accuracy, streak, regionAccuracyMap, sessions ->

        val totalTimePlayedMs = sessions.sumOf { it.durationMs }
        val averageScore = if (sessions.isNotEmpty()) sessions.sumOf { it.score } / sessions.size else 0
        val recentSessions = sessions.take(RECENT_SESSIONS_LIMIT).map { entity ->
            RecentQuizSession(
                sessionId = entity.id,
                completedAt = entity.completedAt,
                region = entity.region,
                gameMode = entity.gameMode,
                score = entity.score,
                correctAnswers = entity.correctAnswers,
                totalQuestions = entity.totalQuestions,
            )
        }

        StatsUiState(
            isLoading = false,
            totalQuizzes = sessions.size,
            questionsAnswered = totalAnswered,
            overallAccuracyPercent = ((accuracy ?: 0f) * PERCENT_MULTIPLIER).toInt(),
            currentStreak = streak,
            totalTimePlayedMs = totalTimePlayedMs,
            averageScore = averageScore,
            regionAccuracies = regionAccuracyMap,
            recentSessions = recentSessions,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
        initialValue = StatsUiState(isLoading = true),
    )

    companion object {
        /** Number of most-recent sessions to surface in the Recent Quizzes list. */
        const val RECENT_SESSIONS_LIMIT = 10

        /** Rolling window for streak and region-accuracy queries (in days). */
        private const val STREAK_WINDOW_DAYS = 30L

        /** Multiplier to convert a 0–1 accuracy fraction to a 0–100 integer. */
        private const val PERCENT_MULTIPLIER = 100f

        /** Delay in ms before upstream flows are cancelled after the last subscriber. */
        private const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
