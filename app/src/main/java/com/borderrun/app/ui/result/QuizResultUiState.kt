package com.borderrun.app.ui.result

import com.borderrun.app.domain.model.QuizResult

/**
 * Sealed UI state for the Quiz Result screen.
 *
 * Transitions: [Loading] → [Success] (data loaded) or [Error] (session missing).
 */
sealed class QuizResultUiState {

    /** Shown while [GetQuizResultUseCase] is running. */
    data object Loading : QuizResultUiState()

    /**
     * Session not found in Room or an unexpected exception occurred.
     *
     * @property message Human-readable description shown to the user.
     */
    data class Error(val message: String) : QuizResultUiState()

    /**
     * Result data loaded successfully.
     *
     * @property result Aggregate result containing session metadata and
     *   per-question answer summaries.
     */
    data class Success(val result: QuizResult) : QuizResultUiState()
}
