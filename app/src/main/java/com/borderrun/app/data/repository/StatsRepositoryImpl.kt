package com.borderrun.app.data.repository

import com.borderrun.app.data.local.dao.QuizAnswerDao
import com.borderrun.app.data.local.dao.QuizSessionDao
import com.borderrun.app.domain.model.WeaknessInfo
import com.borderrun.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Concrete implementation of [StatsRepository].
 *
 * Delegates all reads to Room DAOs ([QuizSessionDao], [QuizAnswerDao]).
 * Maps data-layer projection types to domain-layer types where necessary to
 * keep the domain layer free of data-layer dependencies.
 *
 * @property quizSessionDao DAO for the `quiz_sessions` table.
 * @property quizAnswerDao DAO for the `quiz_answers` table.
 */
class StatsRepositoryImpl @Inject constructor(
    private val quizSessionDao: QuizSessionDao,
    private val quizAnswerDao: QuizAnswerDao,
) : StatsRepository {

    /**
     * Delegates to [QuizSessionDao.getActiveDays].
     *
     * @param since Unix timestamp (ms) lower bound for the query.
     */
    override fun getStreak(since: Long): Flow<Int> =
        quizSessionDao.getActiveDays(since)

    /**
     * Delegates to [QuizSessionDao.getTotalQuestionsAnswered], mapping `null`
     * (empty table) to `0`.
     */
    override fun getTotalAnswered(): Flow<Int> =
        quizSessionDao.getTotalQuestionsAnswered().map { it ?: 0 }

    /** Delegates to [QuizAnswerDao.getOverallAccuracy]. */
    override fun getOverallAccuracy(): Flow<Float?> =
        quizAnswerDao.getOverallAccuracy()

    /**
     * Delegates to [QuizAnswerDao.getWeakestRegion] and maps the
     * data-layer [com.borderrun.app.data.local.model.RegionAccuracy] to the
     * domain-layer [WeaknessInfo].
     *
     * @param since Unix timestamp (ms) — typically `now - 30 days`.
     */
    override fun getWeakestRegion(since: Long): Flow<WeaknessInfo?> =
        quizAnswerDao.getWeakestRegion(since).map { regionAccuracy ->
            regionAccuracy?.let {
                WeaknessInfo(
                    regionName = it.region,
                    accuracyFraction = it.accuracyFraction,
                )
            }
        }

    /**
     * Delegates to [QuizAnswerDao.getWeeklyActivity] and converts the list of
     * [com.borderrun.app.data.local.model.DayActivity] rows to a `date → count` map.
     *
     * @param weekStart Unix timestamp (ms) at Monday midnight UTC.
     */
    override fun getWeeklyActivity(weekStart: Long): Flow<Map<String, Int>> =
        quizAnswerDao.getWeeklyActivity(weekStart).map { activities ->
            activities.associate { it.day to it.count }
        }

    /** Delegates to [QuizSessionDao.getStreakHighScore]. */
    override fun getStreakHighScore(): Flow<Int?> =
        quizSessionDao.getStreakHighScore()

    /**
     * Uses [QuizSessionDao.getTotalQuestionsAnswered] as a proxy for total
     * answer count (each session records one answer row per question presented).
     */
    override fun getTotalAnswerCount(): Flow<Int?> =
        quizSessionDao.getTotalQuestionsAnswered()

    /**
     * Delegates to [QuizAnswerDao.getAccuracyByRegion] and converts the
     * list of [com.borderrun.app.data.local.model.RegionAccuracy] rows into a
     * `region → accuracy fraction` map, keeping the domain layer free of
     * data-layer types.
     *
     * @param since Unix timestamp (ms) — typically `now - 30 days`.
     */
    override fun getAccuracyByRegion(since: Long): Flow<Map<String, Float>> =
        quizAnswerDao.getAccuracyByRegion(since).map { accuracies ->
            accuracies.associate { it.region to it.accuracyFraction }
        }
}
