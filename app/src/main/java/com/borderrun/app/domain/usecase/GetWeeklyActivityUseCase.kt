package com.borderrun.app.domain.usecase

import com.borderrun.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case that returns per-day answer activity for the current week.
 *
 * Wraps [StatsRepository.getWeeklyActivity] to follow the "ViewModels call
 * use cases, not repositories directly" contract.
 *
 * @property statsRepository Source of weekly activity data.
 */
class GetWeeklyActivityUseCase @Inject constructor(
    private val statsRepository: StatsRepository,
) {

    /**
     * Returns a [Flow] emitting a `"YYYY-MM-DD" → count` map for all days
     * with activity since [weekStart].
     *
     * Days not present in the map had zero answers. The Home screen uses this
     * to color the Mon–Fri face icons.
     *
     * @param weekStart Unix timestamp (ms) at Monday midnight UTC for the
     *   desired week.
     * @return Flow emitting the date-to-count activity map.
     */
    operator fun invoke(weekStart: Long): Flow<Map<String, Int>> =
        statsRepository.getWeeklyActivity(weekStart)
}
