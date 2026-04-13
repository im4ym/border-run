package com.borderrun.app.data.repository

import com.borderrun.app.data.local.dao.QuizAnswerDao
import com.borderrun.app.data.local.dao.QuizSessionDao
import com.borderrun.app.data.local.entity.QuizAnswerEntity
import com.borderrun.app.data.local.entity.QuizSessionEntity
import com.borderrun.app.domain.repository.QuizRepository
import javax.inject.Inject

/**
 * Concrete implementation of [QuizRepository] backed by Room DAOs.
 *
 * Sessions are created as placeholder rows at quiz start so that answer rows
 * can reference them via foreign key. [completeSession] fills in the final
 * totals once all questions have been answered.
 *
 * @property quizSessionDao DAO for the `quiz_sessions` table.
 * @property quizAnswerDao DAO for the `quiz_answers` table.
 */
class QuizRepositoryImpl @Inject constructor(
    private val quizSessionDao: QuizSessionDao,
    private val quizAnswerDao: QuizAnswerDao,
) : QuizRepository {

    /**
     * Inserts a placeholder session row and returns its generated ID.
     *
     * @param region Region filter, or `null` for mixed modes.
     * @param difficulty Difficulty string.
     * @param gameMode Game mode string.
     * @return Auto-generated session primary key.
     */
    override suspend fun startSession(
        region: String?,
        difficulty: String,
        gameMode: String,
    ): Int {
        val id = quizSessionDao.insertSession(
            QuizSessionEntity(
                gameMode = gameMode,
                region = region,
                difficulty = difficulty,
                totalQuestions = 0,
                correctAnswers = 0,
                score = 0,
                durationMs = 0,
                completedAt = System.currentTimeMillis(),
            ),
        )
        return id.toInt()
    }

    /**
     * Persists one [QuizAnswerEntity] row for the given question result.
     */
    override suspend fun recordAnswer(
        sessionId: Int,
        questionType: String,
        countryId: String,
        userAnswer: String,
        correctAnswer: String,
        isCorrect: Boolean,
        timeSpentMs: Long,
    ) {
        quizAnswerDao.insertAnswers(
            listOf(
                QuizAnswerEntity(
                    sessionId = sessionId,
                    questionType = questionType,
                    countryId = countryId,
                    userAnswer = userAnswer,
                    correctAnswer = correctAnswer,
                    isCorrect = isCorrect,
                    timeSpentMs = timeSpentMs,
                    answeredAt = System.currentTimeMillis(),
                ),
            ),
        )
    }

    /**
     * Updates the session row with final quiz results.
     */
    override suspend fun completeSession(
        sessionId: Int,
        correctAnswers: Int,
        totalQuestions: Int,
        score: Int,
        durationMs: Long,
    ) {
        quizSessionDao.updateSessionResult(
            id = sessionId,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            score = score,
            durationMs = durationMs,
            completedAt = System.currentTimeMillis(),
        )
    }
}
