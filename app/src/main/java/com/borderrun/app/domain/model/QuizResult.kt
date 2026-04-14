package com.borderrun.app.domain.model

/**
 * Summary of one answered question, used on the Quiz Result screen.
 *
 * @property questionNumber 1-based position in the quiz (1–10).
 * @property questionType One of the [QuestionType] constants — used to derive a
 *   human-readable label since the full question text is not persisted to Room.
 * @property userAnswer The option string the user selected.
 * @property correctAnswer The correct answer string.
 * @property isCorrect Whether [userAnswer] equals [correctAnswer].
 * @property timeSpentMs Time the user spent on this question in milliseconds.
 */
data class QuizAnswerSummary(
    val questionNumber: Int,
    val questionType: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timeSpentMs: Long,
)

/**
 * Aggregate result for a completed quiz session.
 *
 * Combines data from [com.borderrun.app.data.local.entity.QuizSessionEntity]
 * and a list of [com.borderrun.app.data.local.entity.QuizAnswerEntity] rows,
 * mapped to domain types by [com.borderrun.app.data.repository.QuizRepositoryImpl].
 *
 * @property sessionId Room primary key of the session.
 * @property region Region filter used for this quiz, or `null` for mixed modes.
 * @property difficulty One of `"easy"`, `"medium"`, `"hard"`.
 * @property totalQuestions Total number of questions presented.
 * @property correctAnswers Number of correctly answered questions.
 * @property score Total points earned.
 * @property durationMs Total quiz duration in milliseconds.
 * @property answers Per-question answer summaries in order.
 */
data class QuizResult(
    val sessionId: Int,
    val region: String?,
    val difficulty: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Int,
    val durationMs: Long,
    val answers: List<QuizAnswerSummary>,
) {
    /** Accuracy fraction in `0.0..1.0`. */
    val accuracy: Float
        get() = if (totalQuestions == 0) 0f
        else correctAnswers.toFloat() / totalQuestions

    /** Accuracy as a 0–100 integer percentage. */
    val accuracyPercent: Int
        get() = (accuracy * 100).toInt()
}
