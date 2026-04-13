package com.borderrun.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.borderrun.app.data.local.entity.QuizAnswerEntity
import com.borderrun.app.data.local.model.DayActivity
import com.borderrun.app.data.local.model.QuestionTypeAccuracy
import com.borderrun.app.data.local.model.RegionAccuracy
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `quiz_answers` table.
 *
 * Contains the complex analytical queries used by the Statistics screen and
 * the adaptive difficulty system.
 */
@Dao
interface QuizAnswerDao {

    /**
     * Inserts a list of [QuizAnswerEntity] rows for a just-completed session.
     *
     * @param answers All answers from a single quiz session.
     */
    @Insert
    suspend fun insertAnswers(answers: List<QuizAnswerEntity>)

    /**
     * Returns all answers for a specific session, ordered by time answered.
     *
     * @param sessionId The parent session primary key.
     * @return List of answers (not a Flow — used once on the Result screen).
     */
    @Query("SELECT * FROM quiz_answers WHERE sessionId = :sessionId ORDER BY answeredAt ASC")
    suspend fun getAnswersForSession(sessionId: Int): List<QuizAnswerEntity>

    /**
     * Returns accuracy grouped by region for the last 30 days.
     *
     * Joins with `countries` to resolve [QuizAnswerEntity.countryId] → region.
     * Results are emitted as [RegionAccuracy] projections containing total and
     * correct counts per region.
     *
     * @param since Unix timestamp (ms) — typically `now - 30 days`.
     * @return Flow emitting a list of [RegionAccuracy] rows.
     */
    @Query("""
        SELECT c.region,
               COUNT(*) AS total,
               SUM(CASE WHEN qa.isCorrect THEN 1 ELSE 0 END) AS correct
        FROM quiz_answers qa
        JOIN countries c ON qa.countryId = c.id
        WHERE qa.answeredAt > :since
        GROUP BY c.region
    """)
    fun getAccuracyByRegion(since: Long): Flow<List<RegionAccuracy>>

    /**
     * Returns the single weakest region (lowest accuracy) over the period.
     *
     * @param since Unix timestamp (ms) — typically `now - 30 days`.
     * @return Flow emitting the weakest [RegionAccuracy], or `null` if no data.
     */
    @Query("""
        SELECT c.region,
               COUNT(*) AS total,
               SUM(CASE WHEN qa.isCorrect THEN 1 ELSE 0 END) AS correct
        FROM quiz_answers qa
        JOIN countries c ON qa.countryId = c.id
        WHERE qa.answeredAt > :since
        GROUP BY c.region
        ORDER BY CAST(SUM(CASE WHEN qa.isCorrect THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) ASC
        LIMIT 1
    """)
    fun getWeakestRegion(since: Long): Flow<RegionAccuracy?>

    /**
     * Returns daily answer counts since [weekStart] for the weekly activity chart.
     *
     * Each [DayActivity] row contains a `"YYYY-MM-DD"` date string and a count.
     *
     * @param weekStart Unix timestamp (ms) at the start of the week (Monday midnight UTC).
     * @return Flow emitting the list of [DayActivity] rows in ascending date order.
     */
    @Query("""
        SELECT date(answeredAt/1000, 'unixepoch') AS day, COUNT(*) AS count
        FROM quiz_answers
        WHERE answeredAt > :weekStart
        GROUP BY day
        ORDER BY day ASC
    """)
    fun getWeeklyActivity(weekStart: Long): Flow<List<DayActivity>>

    /**
     * Returns accuracy grouped by question type across all time.
     *
     * Used by the adaptive difficulty algorithm to identify which question
     * types the user struggles with.
     *
     * @return Flow emitting a list of [QuestionTypeAccuracy] rows.
     */
    @Query("""
        SELECT questionType,
               CAST(SUM(CASE WHEN isCorrect THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) AS accuracy
        FROM quiz_answers
        GROUP BY questionType
    """)
    fun getAccuracyByQuestionType(): Flow<List<QuestionTypeAccuracy>>

    /**
     * Returns the overall accuracy across all recorded answers.
     *
     * @return Flow emitting accuracy as a [Float] in `0.0..1.0`, or `null` if empty.
     */
    @Query("""
        SELECT CAST(SUM(CASE WHEN isCorrect THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*)
        FROM quiz_answers
    """)
    fun getOverallAccuracy(): Flow<Float?>

    /**
     * Deletes all rows from the `quiz_answers` table.
     *
     * Called from "Clear All My Data" in Settings.
     */
    @Query("DELETE FROM quiz_answers")
    suspend fun deleteAll()
}
