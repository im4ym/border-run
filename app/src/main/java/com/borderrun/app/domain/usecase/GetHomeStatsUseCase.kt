package com.borderrun.app.domain.usecase

import com.borderrun.app.domain.model.HomeStats
import com.borderrun.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Number of milliseconds in 30 days, used as the streak look-back window. */
private const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1_000

/**
 * Use case that aggregates the four statistical signals required by the
 * Home screen into a single [HomeStats] stream.
 *
 * Combines:
 * - streak (active days in the last 30 days)
 * - total questions answered
 * - overall answer accuracy
 * - streak mode high score
 *
 * @property statsRepository Source of all statistics data.
 */
class GetHomeStatsUseCase @Inject constructor(
    private val statsRepository: StatsRepository,
) {

    /**
     * Returns a [Flow] that emits a fresh [HomeStats] whenever any of the
     * underlying statistics change.
     *
     * The look-back window for the streak is always the most recent 30 days
     * measured from the moment the flow is first collected.
     *
     * @return Flow emitting [HomeStats].
     */
    operator fun invoke(): Flow<HomeStats> {
        val since = System.currentTimeMillis() - THIRTY_DAYS_MS
        return combine(
            statsRepository.getStreak(since),
            statsRepository.getTotalAnswered(),
            statsRepository.getOverallAccuracy(),
            statsRepository.getStreakHighScore(),
        ) { streak, totalAnswered, accuracy, highScore ->
            HomeStats(
                streak = streak,
                totalAnswered = totalAnswered,
                accuracy = accuracy ?: 0f,
                streakHighScore = highScore ?: 0,
            )
        }
    }
}
