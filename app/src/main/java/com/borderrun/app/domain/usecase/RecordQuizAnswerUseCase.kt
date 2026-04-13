package com.borderrun.app.domain.usecase

import com.borderrun.app.domain.repository.QuizRepository
import javax.inject.Inject

/**
 * Persists the user's answer for a single quiz question to Room.
 *
 * Called by [com.borderrun.app.ui.quiz.QuizViewModel] immediately after the user
 * selects an answer, so that partial progress is not lost if the app is
 * backgrounded mid-quiz.
 *
 * @property quizRepository Writes the answer row to the `quiz_answers` table.
 */
class RecordQuizAnswerUseCase @Inject constructor(
    private val quizRepository: QuizRepository,
) {

    /**
     * Executes the use case.
     *
     * @param sessionId Session primary key returned by [GenerateQuizUseCase].
     * @param questionType One of the [com.borderrun.app.domain.model.QuestionType] constants.
     * @param countryId cca3 of the primary country in the question.
     * @param userAnswer The option string the user selected.
     * @param correctAnswer The correct answer string.
     * @param isCorrect Whether [userAnswer] equals [correctAnswer].
     * @param timeSpentMs Milliseconds the user spent on this question.
     */
    suspend operator fun invoke(
        sessionId: Int,
        questionType: String,
        countryId: String,
        userAnswer: String,
        correctAnswer: String,
        isCorrect: Boolean,
        timeSpentMs: Long,
    ) {
        quizRepository.recordAnswer(
            sessionId = sessionId,
            questionType = questionType,
            countryId = countryId,
            userAnswer = userAnswer,
            correctAnswer = correctAnswer,
            isCorrect = isCorrect,
            timeSpentMs = timeSpentMs,
        )
    }
}
