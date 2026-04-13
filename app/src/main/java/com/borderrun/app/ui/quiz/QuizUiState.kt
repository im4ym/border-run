package com.borderrun.app.ui.quiz

import com.borderrun.app.domain.model.QuizQuestion

/**
 * Sealed UI state hierarchy for the Quiz screen.
 *
 * Transitions: [Loading] → [Active] (questions loaded) → [Complete] (all answered).
 * [Error] is a terminal state reached when [GenerateQuizUseCase] fails.
 */
sealed class QuizUiState {

    /** Initial state while [GenerateQuizUseCase] is running. */
    data object Loading : QuizUiState()

    /**
     * Terminal error state — country data unavailable or pool too small.
     *
     * @property message Human-readable description shown to the user.
     */
    data class Error(val message: String) : QuizUiState()

    /**
     * Active quiz state — questions are loaded and the user is answering.
     *
     * @property questions All questions for this session.
     * @property currentIndex Zero-based index of the question being shown.
     * @property selectedAnswer The answer the user tapped, or `null` if not yet
     *   answered.
     * @property isAnswerRevealed `true` after [selectedAnswer] is submitted —
     *   triggers feedback coloring and shows the explanation.
     * @property score Cumulative points earned so far.
     * @property correctCount Number of correct answers so far.
     * @property sessionId Room primary key of the in-progress session.
     * @property elapsedSeconds Total seconds elapsed since the quiz started
     *   (incremented by the ViewModel's timer coroutine).
     * @property questionStartTimeMs System timestamp when the current question
     *   was first shown; used to compute per-question time spent.
     */
    data class Active(
        val questions: List<QuizQuestion>,
        val currentIndex: Int = 0,
        val selectedAnswer: String? = null,
        val isAnswerRevealed: Boolean = false,
        val score: Int = 0,
        val correctCount: Int = 0,
        val sessionId: Int = INVALID_SESSION_ID,
        val elapsedSeconds: Int = 0,
        val questionStartTimeMs: Long = System.currentTimeMillis(),
    ) : QuizUiState() {

        /** The question currently being displayed. */
        val currentQuestion: QuizQuestion?
            get() = questions.getOrNull(currentIndex)

        /** Progress fraction in `0.0..1.0` for the progress bar. */
        val progress: Float
            get() = if (questions.isEmpty()) 0f
            else (currentIndex + 1).toFloat() / questions.size

        /** `true` when [currentIndex] is at the last question. */
        val isLastQuestion: Boolean
            get() = currentIndex >= questions.size - 1

        /** `true` when the last answered question was correct. */
        val lastAnswerCorrect: Boolean
            get() = isAnswerRevealed && selectedAnswer == currentQuestion?.correctAnswer
    }

    /**
     * Terminal state — all questions answered; triggers navigation to the
     * Quiz Result screen.
     *
     * @property sessionId Room primary key of the completed session, passed
     *   as a navigation argument to the Result screen.
     */
    data class Complete(val sessionId: Int) : QuizUiState()

    companion object {
        private const val INVALID_SESSION_ID = -1
    }
}
