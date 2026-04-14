package com.borderrun.app.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.borderrun.app.domain.model.Country
import com.borderrun.app.domain.model.QuestionType
import com.borderrun.app.domain.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [QuizActiveContent].
 *
 * We call [QuizActiveContent] directly (it is `internal`) with a hand-crafted
 * [QuizUiState.Active] so that no ViewModel or Hilt injection is needed.
 */
class QuizScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Test helpers ──────────────────────────────────────────────────────────

    private fun fakeMultipleChoiceQuestion(
        questionText: String = "What is the capital of TestLand?",
        correctAnswer: String = "TestCity",
        options: List<String> = listOf("TestCity", "OtherCity", "AnotherCity", "FinalCity"),
    ) = QuizQuestion.MultipleChoice(
        questionText = questionText,
        correctAnswer = correctAnswer,
        options = options,
        region = "Asia",
        difficulty = "medium",
        explanationText = "$correctAnswer is the capital of TestLand.",
        questionType = QuestionType.CAPITAL,
        primaryCountryId = "TST",
    )

    private fun fakeTrueFalseQuestion(
        questionText: String = "True or False: TestLand is landlocked.",
        correctAnswer: String = "True",
    ) = QuizQuestion.TrueFalse(
        questionText = questionText,
        correctAnswer = correctAnswer,
        region = "Asia",
        difficulty = "medium",
        explanationText = "TestLand has no coastline.",
        questionType = QuestionType.LANDLOCKED_TF,
        primaryCountryId = "TST",
    )

    private fun fakeCountry(id: String, name: String, population: Long = 1_000_000L) = Country(
        id = id,
        name = name,
        officialName = "Official $name",
        capital = "${name}City",
        region = "Asia",
        subregion = "South Asia",
        flagUrl = "https://flag.example.com/$id.png",
        population = population,
        area = 100_000.0,
        languages = listOf("English"),
        currencies = listOf("Dollar"),
        borders = emptyList(),
        isLandlocked = false,
        drivingSide = "right",
    )

    /**
     * Renders [QuizActiveContent] inside a gradient background that mimics the
     * actual screen, since [QuizActiveContent] uses a transparent scaffold.
     */
    private fun setQuizContent(
        state: QuizUiState.Active,
        onNavigateBack: () -> Unit = {},
        onAnswerSelected: (String) -> Unit = {},
        onNextQuestion: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF1A3C2E), Color(0xFF0D2B1E))),
                    ),
            ) {
                QuizActiveContent(
                    state = state,
                    onNavigateBack = onNavigateBack,
                    onAnswerSelected = onAnswerSelected,
                    onNextQuestion = onNextQuestion,
                )
            }
        }
    }

    // ── Question text display ─────────────────────────────────────────────────

    @Test
    fun questionTextIsDisplayed() {
        val question = fakeMultipleChoiceQuestion(
            questionText = "What is the capital of TestLand?",
        )
        setQuizContent(
            state = QuizUiState.Active(
                questions = listOf(question),
                currentIndex = 0,
            ),
        )

        composeTestRule
            .onNodeWithText("What is the capital of TestLand?")
            .assertIsDisplayed()
    }

    @Test
    fun trueFalseQuestionTextIsDisplayed() {
        val question = fakeTrueFalseQuestion(
            questionText = "True or False: TestLand is landlocked.",
        )
        setQuizContent(
            state = QuizUiState.Active(
                questions = listOf(question),
                currentIndex = 0,
            ),
        )

        composeTestRule
            .onNodeWithText("True or False: TestLand is landlocked.")
            .assertIsDisplayed()
    }

    // ── Answer options ────────────────────────────────────────────────────────

    @Test
    fun multipleChoiceOptionsAreDisplayed() {
        val question = fakeMultipleChoiceQuestion(
            options = listOf("TestCity", "OtherCity", "AnotherCity", "FinalCity"),
        )
        setQuizContent(
            state = QuizUiState.Active(questions = listOf(question)),
        )

        composeTestRule.onNodeWithText("TestCity").assertIsDisplayed()
        composeTestRule.onNodeWithText("OtherCity").assertIsDisplayed()
        composeTestRule.onNodeWithText("AnotherCity").assertIsDisplayed()
        composeTestRule.onNodeWithText("FinalCity").assertIsDisplayed()
    }

    @Test
    fun clickingAnswerOptionInvokesCallback() {
        val question = fakeMultipleChoiceQuestion(
            options = listOf("TestCity", "OtherCity", "AnotherCity", "FinalCity"),
        )
        val selected = mutableListOf<String>()

        setQuizContent(
            state = QuizUiState.Active(questions = listOf(question)),
            onAnswerSelected = { answer -> selected.add(answer) },
        )

        composeTestRule.onNodeWithText("OtherCity").performClick()

        assertEquals(1, selected.size)
        assertEquals("OtherCity", selected[0])
    }

    @Test
    fun trueFalseOptionsTrueAndFalseAreDisplayed() {
        val question = fakeTrueFalseQuestion()
        setQuizContent(
            state = QuizUiState.Active(questions = listOf(question)),
        )

        composeTestRule.onNodeWithText("True").assertIsDisplayed()
        composeTestRule.onNodeWithText("False").assertIsDisplayed()
    }

    // ── Progress counter ──────────────────────────────────────────────────────

    @Test
    fun progressCounterShowsCurrentAndTotalQuestions() {
        val questions = listOf(
            fakeMultipleChoiceQuestion("Q1?"),
            fakeMultipleChoiceQuestion("Q2?"),
            fakeMultipleChoiceQuestion("Q3?"),
        )
        setQuizContent(
            state = QuizUiState.Active(questions = questions, currentIndex = 0),
        )

        // The top bar renders "1 / 3" for the first question
        composeTestRule.onNodeWithText("1 / 3").assertIsDisplayed()
    }

    @Test
    fun progressCounterUpdatesForSecondQuestion() {
        val questions = listOf(
            fakeMultipleChoiceQuestion("Q1?"),
            fakeMultipleChoiceQuestion("Q2?"),
            fakeMultipleChoiceQuestion("Q3?"),
        )
        setQuizContent(
            state = QuizUiState.Active(questions = questions, currentIndex = 1),
        )

        composeTestRule.onNodeWithText("2 / 3").assertIsDisplayed()
    }

    // ── Compare-Two question ──────────────────────────────────────────────────

    @Test
    fun compareTwoQuestionTextIsDisplayed() {
        val countryA = fakeCountry("AAA", "AlphaLand", population = 5_000_000L)
        val countryB = fakeCountry("BBB", "BetaLand", population = 10_000_000L)
        val question = QuizQuestion.CompareTwo(
            questionText = "Which country has a larger population?",
            correctAnswer = "BetaLand",
            countryA = countryA,
            countryB = countryB,
            region = "Asia",
            difficulty = "medium",
            explanationText = "BetaLand has more people.",
            questionType = QuestionType.POPULATION_COMPARE,
            primaryCountryId = "BBB",
        )

        setQuizContent(
            state = QuizUiState.Active(questions = listOf(question)),
        )

        composeTestRule
            .onNodeWithText("Which country has a larger population?")
            .assertIsDisplayed()
    }
}
