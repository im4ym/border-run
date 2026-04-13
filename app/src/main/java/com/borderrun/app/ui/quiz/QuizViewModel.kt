package com.borderrun.app.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borderrun.app.domain.usecase.CompleteQuizSessionUseCase
import com.borderrun.app.domain.usecase.GenerateQuizResult
import com.borderrun.app.domain.usecase.GenerateQuizUseCase
import com.borderrun.app.domain.usecase.RecordQuizAnswerUseCase
import com.borderrun.app.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Quiz screen.
 *
 * On creation, [loadQuiz] generates questions for the [region] and [difficulty]
 * read from [SavedStateHandle] (populated automatically by Navigation Compose).
 *
 * A one-second timer coroutine increments [QuizUiState.Active.elapsedSeconds]
 * while the quiz is active and is cancelled when the session completes or the
 * ViewModel is cleared.
 *
 * @property generateQuizUseCase Loads countries, creates a session, and
 *   generates questions.
 * @property recordQuizAnswerUseCase Persists each answer to Room immediately
 *   after the user taps an option.
 * @property completeQuizSessionUseCase Writes final totals into the session row
 *   when all questions have been answered.
 */
@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val recordQuizAnswerUseCase: RecordQuizAnswerUseCase,
    private val completeQuizSessionUseCase: CompleteQuizSessionUseCase,
) : ViewModel() {

    /** Region read from the navigation route argument. */
    private val region: String =
        savedStateHandle[Screen.Quiz.ARG_REGION] ?: REGION_MIXED

    /** Difficulty read from the navigation route argument. */
    private val difficulty: String =
        savedStateHandle[Screen.Quiz.ARG_DIFFICULTY] ?: DIFFICULTY_MEDIUM

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)

    /** Observed by [QuizScreen] to drive the entire UI. */
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    /** System timestamp when the current quiz started; used for total duration. */
    private var quizStartTimeMs: Long = 0L

    /** Reference to the running timer coroutine so it can be cancelled. */
    private var timerJob: Job? = null

    init {
        loadQuiz()
    }

    // ── Quiz loading ──────────────────────────────────────────────────────────

    /**
     * Generates questions and starts the session. Sets state to [QuizUiState.Active]
     * on success or [QuizUiState.Error] on failure.
     */
    private fun loadQuiz() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            when (val result = generateQuizUseCase(region, difficulty)) {
                is GenerateQuizResult.Success -> {
                    quizStartTimeMs = System.currentTimeMillis()
                    _uiState.value = QuizUiState.Active(
                        questions = result.questions,
                        sessionId = result.sessionId,
                        questionStartTimeMs = System.currentTimeMillis(),
                    )
                    startTimer()
                }
                is GenerateQuizResult.Error -> {
                    _uiState.value = QuizUiState.Error(result.message)
                }
            }
        }
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    /**
     * Launches a coroutine that increments [QuizUiState.Active.elapsedSeconds]
     * every second. Stops automatically when the state leaves [QuizUiState.Active].
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(TIMER_TICK_MS)
                val current = _uiState.value as? QuizUiState.Active ?: break
                _uiState.value = current.copy(elapsedSeconds = current.elapsedSeconds + 1)
            }
        }
    }

    // ── Answer handling ───────────────────────────────────────────────────────

    /**
     * Called when the user taps an answer option.
     *
     * Reveals the correct/incorrect feedback and immediately persists the answer
     * to Room via [RecordQuizAnswerUseCase]. No-ops if the answer has already
     * been revealed for the current question.
     *
     * @param answer The option string the user selected.
     */
    fun onAnswerSelected(answer: String) {
        val current = _uiState.value as? QuizUiState.Active ?: return
        if (current.isAnswerRevealed) return
        val question = current.currentQuestion ?: return

        val isCorrect = answer == question.correctAnswer
        val timeSpentMs = System.currentTimeMillis() - current.questionStartTimeMs

        _uiState.value = current.copy(
            selectedAnswer = answer,
            isAnswerRevealed = true,
            score = if (isCorrect) current.score + POINTS_PER_CORRECT else current.score,
            correctCount = if (isCorrect) current.correctCount + 1 else current.correctCount,
        )

        viewModelScope.launch {
            recordQuizAnswerUseCase(
                sessionId = current.sessionId,
                questionType = question.questionType,
                countryId = question.primaryCountryId,
                userAnswer = answer,
                correctAnswer = question.correctAnswer,
                isCorrect = isCorrect,
                timeSpentMs = timeSpentMs,
            )
        }
    }

    /**
     * Advances to the next question or, after the last question, completes the
     * session and transitions to [QuizUiState.Complete].
     *
     * No-ops if the current answer has not yet been revealed.
     */
    fun onNextQuestion() {
        val current = _uiState.value as? QuizUiState.Active ?: return
        if (!current.isAnswerRevealed) return

        if (current.isLastQuestion) {
            timerJob?.cancel()
            val durationMs = System.currentTimeMillis() - quizStartTimeMs
            viewModelScope.launch {
                completeQuizSessionUseCase(
                    sessionId = current.sessionId,
                    correctAnswers = current.correctCount,
                    totalQuestions = current.questions.size,
                    score = current.score,
                    durationMs = durationMs,
                )
                _uiState.value = QuizUiState.Complete(current.sessionId)
            }
        } else {
            _uiState.value = current.copy(
                currentIndex = current.currentIndex + 1,
                selectedAnswer = null,
                isAnswerRevealed = false,
                questionStartTimeMs = System.currentTimeMillis(),
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        /** Points awarded for each correct answer. */
        const val POINTS_PER_CORRECT = 10

        /** Timer tick interval in milliseconds. */
        private const val TIMER_TICK_MS = 1_000L

        private const val REGION_MIXED = "mixed"
        private const val DIFFICULTY_MEDIUM = "medium"
    }
}
