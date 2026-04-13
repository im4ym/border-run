package com.borderrun.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.borderrun.app.data.local.entity.QuizSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `quiz_sessions` table.
 */
@Dao
interface QuizSessionDao {

    /**
     * Inserts a completed [QuizSessionEntity] and returns its generated row ID.
     *
     * @param session The session to persist.
     * @return The auto-generated [QuizSessionEntity.id] for the new row.
     */
    @Insert
    suspend fun insertSession(session: QuizSessionEntity): Long

    /**
     * Returns the session with the given [sessionId], or `null` if not found.
     *
     * @param sessionId The primary key of the session to fetch.
     */
    @Query("SELECT * FROM quiz_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Int): QuizSessionEntity?

    /**
     * Returns all sessions ordered by completion time (newest first).
     *
     * @return Flow emitting the full session list.
     */
    @Query("SELECT * FROM quiz_sessions ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<QuizSessionEntity>>

    /**
     * Returns sessions for a specific [gameMode].
     *
     * @param gameMode One of `"classic"`, `"daily"`, `"streak"`, `"speed"`.
     */
    @Query("SELECT * FROM quiz_sessions WHERE gameMode = :gameMode ORDER BY completedAt DESC")
    fun getSessionsByMode(gameMode: String): Flow<List<QuizSessionEntity>>

    /**
     * Returns the number of distinct active days since [since] (Unix ms).
     *
     * An "active day" is any UTC calendar date on which at least one session
     * was completed. Used to calculate the current streak.
     *
     * @param since Unix timestamp in milliseconds (typically 30 days ago).
     * @return Flow emitting the count of active days.
     */
    @Query("""
        SELECT COUNT(DISTINCT date(completedAt/1000, 'unixepoch'))
        FROM quiz_sessions
        WHERE completedAt > :since
    """)
    fun getActiveDays(since: Long): Flow<Int>

    /**
     * Returns the total number of correct answers across all sessions.
     *
     * @return Flow emitting the cumulative correct answer count.
     */
    @Query("SELECT SUM(correctAnswers) FROM quiz_sessions")
    fun getTotalCorrectAnswers(): Flow<Int?>

    /**
     * Returns the total number of questions answered across all sessions.
     *
     * @return Flow emitting the cumulative question count.
     */
    @Query("SELECT SUM(totalQuestions) FROM quiz_sessions")
    fun getTotalQuestionsAnswered(): Flow<Int?>

    /**
     * Returns the highest score ever achieved in streak mode.
     *
     * @return Flow emitting the maximum streak score, or `null` if none exists.
     */
    @Query("SELECT MAX(score) FROM quiz_sessions WHERE gameMode = 'streak'")
    fun getStreakHighScore(): Flow<Int?>

    /**
     * Deletes all rows from the `quiz_sessions` table.
     *
     * Called from "Clear All My Data" in Settings.
     */
    @Query("DELETE FROM quiz_sessions")
    suspend fun deleteAll()
}
