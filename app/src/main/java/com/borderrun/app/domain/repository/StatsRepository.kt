package com.borderrun.app.domain.repository

import com.borderrun.app.domain.model.WeaknessInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for all quiz-statistics data, defined in the domain layer.
 *
 * The concrete implementation ([com.borderrun.app.data.repository.StatsRepositoryImpl])
 * lives in the data layer and is provided via Hilt [RepositoryModule].
 *
 * All functions return [Flow] so that ViewModels and use cases react
 * automatically to database changes without manual refresh calls.
 */
interface StatsRepository {

    /**
     * Returns the number of distinct UTC calendar days on which at least one
     * quiz session was completed, within the given time window.
     *
     * Used to calculate the current day streak.
     *
     * @param since Unix timestamp (ms). Typically `now - 30 days`.
     * @return Flow emitting the count of active days.
     */
    fun getStreak(since: Long): Flow<Int>

    /**
     * Returns the cumulative total of questions answered across all sessions.
     *
     * Emits `0` when no sessions have been recorded yet.
     *
     * @return Flow emitting the total question count.
     */
    fun getTotalAnswered(): Flow<Int>

    /**
     * Returns the overall accuracy across all recorded answers as a fraction.
     *
     * Emits `null` when no answers have been recorded yet.
     *
     * @return Flow emitting the accuracy in `0.0..1.0`, or `null`.
     */
    fun getOverallAccuracy(): Flow<Float?>

    /**
     * Returns the weakest region (lowest accuracy) over the given time window.
     *
     * Emits `null` when there is not enough data to determine a weakest region.
     *
     * @param since Unix timestamp (ms) — typically `now - 30 days`.
     * @return Flow emitting [WeaknessInfo] for the weakest region, or `null`.
     */
    fun getWeakestRegion(since: Long): Flow<WeaknessInfo?>

    /**
     * Returns per-day answer activity since [weekStart] as a date→count map.
     *
     * Keys are date strings in `"YYYY-MM-DD"` format (UTC). Missing dates mean
     * zero activity on that day.
     *
     * @param weekStart Unix timestamp (ms) at the Monday midnight of the desired week (UTC).
     * @return Flow emitting the activity map.
     */
    fun getWeeklyActivity(weekStart: Long): Flow<Map<String, Int>>

    /**
     * Returns the highest score ever achieved in Streak game mode.
     *
     * Emits `null` when no streak sessions have been recorded.
     *
     * @return Flow emitting the streak high score, or `null`.
     */
    fun getStreakHighScore(): Flow<Int?>

    /**
     * Returns the total number of answers recorded, or `null` if none.
     *
     * Used by [com.borderrun.app.domain.usecase.GetWeaknessDataUseCase] to
     * check the minimum-answer threshold before showing the Weakness Trainer.
     *
     * @return Flow emitting the total answer count, or `null`.
     */
    fun getTotalAnswerCount(): Flow<Int?>
}
