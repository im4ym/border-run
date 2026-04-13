package com.borderrun.app.domain.usecase

import com.borderrun.app.domain.repository.QuizRepository
import javax.inject.Inject

/**
 * Finalises a quiz session by writing the total results into the placeholder
 * row created by [GenerateQuizUseCase].
 *
 * @property quizRepository Updates the session row in Room.
 */
class CompleteQuizSessionUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {

    /**
     * Executes the use case.
     *
     * @param sessionId Session primary key returned by [GenerateQuizUseCase].
     * @param correctAnswers Number of correctly answered questions.
     * @param totalQuestions Total number of questions in the session.
     * @param score Total points earned.
     * @param durationMs Total quiz duration in milliseconds.
     */
    suspend operator fun invoke(
        sessionId: Int,
        correctAnswers: Int,
        totalQuestions: Int,
        score: Int,
        durationMs: Long,
    ) {
        quizRepository.completeSession(
            sessionId = sessionId,
            correctAnswers = correctAnswers,
            totalQuestions = totalQuestions,
            score = score,
            durationMs = durationMs,
        )
    }
}
