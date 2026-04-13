package com.borderrun.app.ui.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.borderrun.app.domain.model.Country
import com.borderrun.app.domain.model.QuizQuestion
import com.borderrun.app.ui.theme.CardBorder
import com.borderrun.app.ui.theme.CardSurface
import com.borderrun.app.ui.theme.CtaGradientEnd
import com.borderrun.app.ui.theme.CtaGradientStart
import com.borderrun.app.ui.theme.ErrorRed
import com.borderrun.app.ui.theme.GradientCyan
import com.borderrun.app.ui.theme.GradientMint
import com.borderrun.app.ui.theme.GradientSky
import com.borderrun.app.ui.theme.GradientTeal
import com.borderrun.app.ui.theme.PrimaryGreen
import com.borderrun.app.ui.theme.SecondaryTeal
import com.borderrun.app.ui.theme.SuccessGreen
import com.borderrun.app.ui.theme.TextBody
import com.borderrun.app.ui.theme.TextHeading
import com.borderrun.app.ui.theme.TextMuted

// ── Layout constants ──────────────────────────────────────────────────────────

private val CARD_PADDING = 20.dp
private val CARD_RADIUS = 22.dp
private val OPTION_RADIUS = 16.dp
private val BADGE_SIZE = 32.dp
private val FLAG_HEIGHT = 180.dp
private val SECTION_SPACING = 12.dp

/** Letters shown in option badge circles. */
private val OPTION_LABELS = listOf("A", "B", "C", "D")

/** Badge background colours per label (before answer reveal). */
private val BADGE_COLORS = listOf(PrimaryGreen, SecondaryTeal, Color(0xFFF59E0B), Color(0xFF8B5CF6))

/**
 * Root composable for the Quiz screen.
 *
 * Collects [QuizViewModel.uiState] and renders the appropriate content for
 * each state: a loading spinner, an error banner, or the active question
 * layout. A [LaunchedEffect] triggers [onNavigateToResult] when the state
 * reaches [QuizUiState.Complete].
 *
 * @param onNavigateToResult Invoked with the completed session ID when the last
 *   question has been answered.
 * @param onNavigateBack Invoked when the user taps the back arrow.
 * @param viewModel Hilt-injected [QuizViewModel].
 */
@Composable
fun QuizScreen(
    onNavigateToResult: (sessionId: Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate to result when quiz is complete.
    val completedState = uiState as? QuizUiState.Complete
    LaunchedEffect(completedState) {
        if (completedState != null) onNavigateToResult(completedState.sessionId)
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(GradientMint, GradientTeal, GradientCyan, GradientSky),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        when (val state = uiState) {
            is QuizUiState.Loading -> QuizLoadingContent()
            is QuizUiState.Error -> QuizErrorContent(message = state.message, onBack = onNavigateBack)
            is QuizUiState.Active -> QuizActiveContent(
                state = state,
                onNavigateBack = onNavigateBack,
                onAnswerSelected = viewModel::onAnswerSelected,
                onNextQuestion = viewModel::onNextQuestion,
            )
            is QuizUiState.Complete -> QuizLoadingContent() // Brief flash while nav fires.
        }
    }
}

// ── Loading / Error ───────────────────────────────────────────────────────────

@Composable
private fun QuizLoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryGreen)
    }
}

@Composable
private fun QuizErrorContent(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("⚠️", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = ErrorRed,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
        ) {
            Text("Go Back")
        }
    }
}

// ── Active quiz content ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizActiveContent(
    state: QuizUiState.Active,
    onNavigateBack: () -> Unit,
    onAnswerSelected: (String) -> Unit,
    onNextQuestion: () -> Unit,
) {
    val question = state.currentQuestion ?: return
    val regionLabel = question.region.takeIf { it.isNotBlank() } ?: "Quiz"

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$regionLabel Quiz",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextHeading,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextHeading,
                        )
                    }
                },
                actions = {
                    // Timer
                    Text(
                        text = state.elapsedSeconds.formatTimer(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextBody,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    // Question counter
                    Text(
                        text = "${state.currentIndex + 1} / ${state.questions.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextHeading,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
        ) {
            // Progress bar
            QuizProgressBar(progress = state.progress)

            // Question card
            QuizQuestionCard(question = question)

            // Answer options
            when (question) {
                is QuizQuestion.MultipleChoice -> MultipleChoiceOptions(
                    question = question,
                    selectedAnswer = state.selectedAnswer,
                    isRevealed = state.isAnswerRevealed,
                    onAnswerSelected = onAnswerSelected,
                )
                is QuizQuestion.TrueFalse -> TrueFalseOptions(
                    question = question,
                    selectedAnswer = state.selectedAnswer,
                    isRevealed = state.isAnswerRevealed,
                    onAnswerSelected = onAnswerSelected,
                )
                is QuizQuestion.CompareTwo -> CompareTwoOptions(
                    question = question,
                    selectedAnswer = state.selectedAnswer,
                    isRevealed = state.isAnswerRevealed,
                    onAnswerSelected = onAnswerSelected,
                )
            }

            // Explanation + score popup after answer reveal
            if (state.isAnswerRevealed) {
                AnswerFeedbackCard(
                    isCorrect = state.lastAnswerCorrect,
                    explanationText = question.explanationText,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Next / Results button — only visible after answering
            if (state.isAnswerRevealed) {
                GradientButton(
                    text = if (state.isLastQuestion) "See Results" else "Next Question",
                    onClick = onNextQuestion,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Progress bar ──────────────────────────────────────────────────────────────

/**
 * Gradient-filled horizontal progress bar showing quiz completion.
 *
 * @param progress Fraction in `0.0..1.0`.
 */
@Composable
private fun QuizProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(CardSurface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(listOf(CtaGradientStart, CtaGradientEnd)),
                ),
        )
    }
}

// ── Question card ─────────────────────────────────────────────────────────────

/**
 * Glass-morphism card displaying the question text and optional flag image.
 *
 * @param question The current [QuizQuestion] to display.
 */
@Composable
private fun QuizQuestionCard(question: QuizQuestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(CARD_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Flag image for flag questions
            if (question is QuizQuestion.MultipleChoice && question.flagUrl != null) {
                AsyncImage(
                    model = question.flagUrl,
                    contentDescription = "Country flag",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(FLAG_HEIGHT)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                )
            }
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleLarge,
                color = TextHeading,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── MultipleChoice options ────────────────────────────────────────────────────

/**
 * Renders four labelled answer option cards for a [QuizQuestion.MultipleChoice].
 *
 * @param question The question whose [QuizQuestion.MultipleChoice.options] are shown.
 * @param selectedAnswer The option the user tapped, or `null`.
 * @param isRevealed Whether correct/incorrect feedback is active.
 * @param onAnswerSelected Callback with the tapped option string.
 */
@Composable
private fun MultipleChoiceOptions(
    question: QuizQuestion.MultipleChoice,
    selectedAnswer: String?,
    isRevealed: Boolean,
    onAnswerSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        question.options.forEachIndexed { index, option ->
            AnswerOptionCard(
                label = OPTION_LABELS.getOrElse(index) { "?" },
                labelColor = BADGE_COLORS.getOrElse(index) { PrimaryGreen },
                text = option,
                isSelected = selectedAnswer == option,
                isCorrect = option == question.correctAnswer,
                isRevealed = isRevealed,
                onClick = { if (!isRevealed) onAnswerSelected(option) },
            )
        }
    }
}

// ── TrueFalse options ─────────────────────────────────────────────────────────

/**
 * Renders "True" and "False" option cards side by side.
 */
@Composable
private fun TrueFalseOptions(
    question: QuizQuestion.TrueFalse,
    selectedAnswer: String?,
    isRevealed: Boolean,
    onAnswerSelected: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("True", "False").forEachIndexed { index, option ->
            AnswerOptionCard(
                label = OPTION_LABELS[index],
                labelColor = BADGE_COLORS[index],
                text = option,
                isSelected = selectedAnswer == option,
                isCorrect = option == question.correctAnswer,
                isRevealed = isRevealed,
                onClick = { if (!isRevealed) onAnswerSelected(option) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ── CompareTwo options ────────────────────────────────────────────────────────

/**
 * Renders two country cards side by side for a [QuizQuestion.CompareTwo] question.
 *
 * Each card shows the country name and population. Tapping a card selects it as
 * the answer.
 */
@Composable
private fun CompareTwoOptions(
    question: QuizQuestion.CompareTwo,
    selectedAnswer: String?,
    isRevealed: Boolean,
    onAnswerSelected: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(question.countryA, question.countryB).forEachIndexed { index, country ->
            CountryCompareCard(
                country = country,
                isSelected = selectedAnswer == country.name,
                isCorrect = country.name == question.correctAnswer,
                isRevealed = isRevealed,
                onClick = { if (!isRevealed) onAnswerSelected(country.name) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * A tappable card representing one country in a [QuizQuestion.CompareTwo] question.
 *
 * @param country The country to display.
 * @param isSelected Whether the user has tapped this card.
 * @param isCorrect Whether this country is the correct answer.
 * @param isRevealed Whether the answer has been revealed.
 * @param onClick Callback when the card is tapped (only propagated before reveal).
 */
@Composable
private fun CountryCompareCard(
    country: Country,
    isSelected: Boolean,
    isCorrect: Boolean,
    isRevealed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (bgColor, borderColor) = revealColors(isSelected, isCorrect, isRevealed)
    Card(
        modifier = modifier.clickable(enabled = !isRevealed, onClick = onClick),
        shape = RoundedCornerShape(OPTION_RADIUS),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AsyncImage(
                model = country.flagUrl,
                contentDescription = "${country.name} flag",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = country.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextHeading,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            // Population is hidden until the answer is revealed to avoid giving it away.
            if (isRevealed) {
                Text(
                    text = country.population.formatPopulation(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextBody,
                )
            }
        }
    }
}

// ── Shared option card ────────────────────────────────────────────────────────

/**
 * A single answer option card with a coloured letter badge.
 *
 * Background and border change after [isRevealed] to show correct (green) /
 * incorrect (red) feedback.
 *
 * @param label Letter shown in the badge ("A", "B", "C", "D").
 * @param labelColor Badge background colour before reveal.
 * @param text The answer option string.
 * @param isSelected Whether the user tapped this option.
 * @param isCorrect Whether this option is the correct answer.
 * @param isRevealed Whether the answer has been revealed.
 * @param onClick Callback when the card is tapped.
 */
@Composable
private fun AnswerOptionCard(
    label: String,
    labelColor: Color,
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isRevealed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (bgColor, borderColor) = revealColors(isSelected, isCorrect, isRevealed)
    val badgeBg = when {
        isRevealed && isCorrect -> SuccessGreen
        isRevealed && isSelected && !isCorrect -> ErrorRed
        else -> labelColor
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isRevealed, onClick = onClick),
        shape = RoundedCornerShape(OPTION_RADIUS),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Letter badge
            Box(
                modifier = Modifier
                    .size(BADGE_SIZE)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = TextHeading,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ── Feedback card ─────────────────────────────────────────────────────────────

/**
 * Displays correct / incorrect feedback and the explanation text after the
 * user submits an answer.
 *
 * @param isCorrect Whether the user's answer was correct.
 * @param explanationText Factual sentence shown regardless of outcome.
 */
@Composable
private fun AnswerFeedbackCard(isCorrect: Boolean, explanationText: String) {
    val icon = if (isCorrect) "✅" else "❌"
    val headline = if (isCorrect) "+${QuizViewModel.POINTS_PER_CORRECT} pts!" else "Not quite!"
    val headlineColor = if (isCorrect) SuccessGreen else ErrorRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(CARD_PADDING),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(icon, fontSize = 20.sp)
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleSmall,
                    color = headlineColor,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = explanationText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextBody,
            )
        }
    }
}

// ── Gradient button ───────────────────────────────────────────────────────────

/**
 * Full-width gradient CTA button.
 *
 * @param text Button label.
 * @param onClick Click callback.
 */
@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(CtaGradientStart, CtaGradientEnd)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Returns the card background and border colours based on reveal state.
 *
 * | Condition                     | Background      | Border     |
 * |-------------------------------|-----------------|------------|
 * | Not yet revealed              | CardSurface     | CardBorder |
 * | Revealed — correct answer     | SuccessGreen 20%| SuccessGreen |
 * | Revealed — user wrong pick    | ErrorRed 20%    | ErrorRed   |
 * | Revealed — other option       | dimmed surface  | CardBorder |
 */
private fun revealColors(
    isSelected: Boolean,
    isCorrect: Boolean,
    isRevealed: Boolean,
): Pair<Color, Color> = when {
    !isRevealed -> CardSurface to CardBorder
    isCorrect -> Color(0xFF059669).copy(alpha = 0.18f) to SuccessGreen
    isSelected -> Color(0xFFE11D48).copy(alpha = 0.18f) to ErrorRed
    else -> CardSurface.copy(alpha = 0.5f) to CardBorder
}

/**
 * Formats elapsed seconds as `mm:ss`.
 *
 * @return String in the format `"02:34"`.
 */
private fun Int.formatTimer(): String {
    val minutes = this / SECONDS_PER_MINUTE
    val seconds = this % SECONDS_PER_MINUTE
    return "%02d:%02d".format(minutes, seconds)
}

/**
 * Formats a [Long] population into a compact human-readable string.
 * (Duplicated from [QuestionGenerator] so the UI layer has no domain dep on the generator.)
 */
private fun Long.formatPopulation(): String = when {
    this >= 1_000_000_000L -> "${"%.1f".format(this / 1_000_000_000.0)}B"
    this >= 1_000_000L -> "${this / 1_000_000}M"
    this >= 1_000L -> "${this / 1_000}K"
    else -> toString()
}

private const val SECONDS_PER_MINUTE = 60
