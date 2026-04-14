package com.borderrun.app.domain.repository

import com.borderrun.app.domain.model.QuizResult

/**
 * Repository interface for quiz session and answer persistence, defined in the
 * domain layer.
 *
 * Implemented by [com.borderrun.app.data.repository.QuizRepositoryImpl] in the
 * data layer and provided via Hilt [RepositoryModule].
 *
 * Lifecycle of a session:
 * 1. [startSession] — inserts a placeholder row; returns the generated session ID.
 * 2. [recordAnswer] — called after each question is answered.
 * 3. [completeSession] — updates the placeholder row with final totals.
 */
interface QuizRepository {

    /**
     * Creates a new quiz session row with placeholder totals and returns its
     * auto-generated primary key.
     *
     * The row is needed upfront so that answer rows (which have a foreign key
     * on `sessionId`) can be written incrementally during the quiz.
     *
     * @param region Region filter used for the quiz, or `null` for mixed modes.
     * @param difficulty One of `"easy"`, `"medium"`, `"hard"`.
     * @param gameMode One of `"classic"`, `"daily"`, `"streak"`, `"speed"`.
     * @return Auto-generated session ID.
     */
    suspend fun startSession(region: String?, difficulty: String, gameMode: String): Int

    /**
     * Persists the user's answer for a single question.
     *
     * @param sessionId Session primary key from [startSession].
     * @param questionType One of the [com.borderrun.app.domain.model.QuestionType] constants.
     * @param countryId cca3 of the primary country in the question.
     * @param userAnswer The option string the user selected.
     * @param correctAnswer The correct answer string.
     * @param isCorrect Whether [userAnswer] equals [correctAnswer].
     * @param timeSpentMs Time the user spent on this question in milliseconds.
     */
    suspend fun recordAnswer(
        sessionId: Int,
        questionType: String,
        countryId: String,
        userAnswer: String,
        correctAnswer: String,
        isCorrect: Boolean,
        timeSpentMs: Long,
    )

    /**
     * Updates the session row created by [startSession] with the final results.
     *
     * @param sessionId Session primary key.
     * @param correctAnswers Number of correctly answered questions.
     * @param totalQuestions Total number of questions in the session.
     * @param score Total points earned.
     * @param durationMs Total quiz duration in milliseconds.
     */
    suspend fun completeSession(
        sessionId: Int,
        correctAnswers: Int,
        totalQuestions: Int,
        score: Int,
        durationMs: Long,
    )

    /**
     * Loads the session and all its answer rows and assembles a [QuizResult].
     *
     * Returns `null` when no session with [sessionId] exists in Room (e.g. bad
     * navigation argument).
     *
     * @param sessionId Session primary key.
     * @return The aggregate [QuizResult], or `null` if not found.
     */
    suspend fun getResult(sessionId: Int): QuizResult?
}
