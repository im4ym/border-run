package com.borderrun.app.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borderrun.app.domain.usecase.GetQuizResultUseCase
import com.borderrun.app.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Quiz Result screen.
 *
 * Reads the [sessionId] from [SavedStateHandle] (populated automatically by
 * Navigation Compose from the `quiz-result/{sessionId}` route), then calls
 * [GetQuizResultUseCase] to load the completed session from Room.
 *
 * @property getQuizResultUseCase Loads session + answers and assembles a
 *   [com.borderrun.app.domain.model.QuizResult].
 */
@HiltViewModel
class QuizResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getQuizResultUseCase: GetQuizResultUseCase,
) : ViewModel() {

    /** Session ID read from the navigation route argument. */
    private val sessionId: Int =
        savedStateHandle[Screen.QuizResult.ARG_SESSION_ID] ?: INVALID_SESSION_ID

    private val _uiState = MutableStateFlow<QuizResultUiState>(QuizResultUiState.Loading)

    /** Observed by [QuizResultScreen] to drive the entire UI. */
    val uiState: StateFlow<QuizResultUiState> = _uiState.asStateFlow()

    init {
        loadResult()
    }

    /**
     * Fetches the quiz result from Room and updates [uiState].
     *
     * Sets [QuizResultUiState.Error] when the session ID is invalid or no
     * matching row exists.
     */
    private fun loadResult() {
        viewModelScope.launch {
            if (sessionId == INVALID_SESSION_ID) {
                _uiState.value = QuizResultUiState.Error("Invalid session.")
                return@launch
            }
            val result = getQuizResultUseCase(sessionId)
            _uiState.value = if (result != null) {
                QuizResultUiState.Success(result)
            } else {
                QuizResultUiState.Error("Session data not found.")
            }
        }
    }

    companion object {
        private const val INVALID_SESSION_ID = -1
    }
}
