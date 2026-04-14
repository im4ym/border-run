package com.borderrun.app.ui.weakness

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borderrun.app.domain.usecase.CompleteQuizSessionUseCase
import com.borderrun.app.domain.usecase.GenerateQuizResult
import com.borderrun.app.domain.usecase.GenerateQuizUseCase
import com.borderrun.app.domain.usecase.RecordQuizAnswerUseCase
import com.borderrun.app.navigation.Screen
import com.borderrun.app.ui.quiz.QuizUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Smart Weakness Trainer screen.
 *
 * Reads the target region from [SavedStateHandle] (set by the NavGraph via
 * [Screen.WeaknessTrainer.ARG_REGION]), then generates a 10-question focused
 * session for that region using [GenerateQuizUseCase].
 *
 * The session is recorded in Room the same way a classic quiz is, so weakness
 * trainer results feed back into the accuracy stats and will improve (or
 * further refine) the identified weak region over time.
 *
 * Answer handling and timer logic mirror [com.borderrun.app.ui.quiz.QuizViewModel]
 * exactly. The only differences are:
 * - The region is provided externally (not from user input).
 * - Difficulty is fixed to `"medium"`.
 *
 * @property generateQuizUseCase Loads countries, creates a session row, and
 *   generates questions for the target region.
 * @property recordQuizAnswerUseCase Persists each answer to Room after it is
 *   submitted.
 * @property completeQuizSessionUseCase Writes final session totals when the
 *   last question is answered.
 */
@HiltViewModel
class WeaknessTrainerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val recordQuizAnswerUseCase: RecordQuizAnswerUseCase,
    private val completeQuizSessionUseCase: CompleteQuizSessionUseCase,
) : ViewModel() {

    /** The weak region to drill — read from the navigation route argument. */
    val region: String =
        checkNotNull(savedStateHandle[Screen.WeaknessTrainer.ARG_REGION]) {
            "WeaknessTrainer requires a '${Screen.WeaknessTrainer.ARG_REGION}' nav argument"
        }

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)

    /** Observed by [WeaknessTrainerScreen] to drive the entire UI. */
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var quizStartTimeMs: Long = 0L
    private var timerJob: Job? = null

    init {
        loadQuiz()
    }

    // ── Quiz loading ──────────────────────────────────────────────────────────

    private fun loadQuiz() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            when (val result = generateQuizUseCase(region, DIFFICULTY)) {
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
     * No-ops if the answer for the current question has already been revealed.
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
     * Advances to the next question or completes the session after the last one.
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
        /** Fixed difficulty level for weakness training sessions. */
        private const val DIFFICULTY = "medium"

        /** Points awarded for each correct answer. */
        private const val POINTS_PER_CORRECT = 10

        /** Timer tick interval in milliseconds. */
        private const val TIMER_TICK_MS = 1_000L
    }
}
