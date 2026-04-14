package com.borderrun.app.ui.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borderrun.app.domain.model.QuizAnswerSummary
import com.borderrun.app.domain.model.QuizResult
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
import com.borderrun.app.ui.theme.SuccessGreen
import com.borderrun.app.ui.theme.TextBody
import com.borderrun.app.ui.theme.TextHeading
import com.borderrun.app.ui.theme.TextMuted

// ── Layout constants ──────────────────────────────────────────────────────────

private val CARD_RADIUS = 22.dp
private val STAT_CARD_RADIUS = 16.dp
private val SECTION_SPACING = 16.dp
private val CARD_PADDING = 20.dp
private val RING_SIZE = 180.dp
private val RING_STROKE = 16.dp

/**
 * Root composable for the Quiz Result screen.
 *
 * Collects [QuizResultViewModel.uiState] and renders either a loading spinner,
 * an error message, or the full result layout with score ring, stats, per-question
 * breakdown, and navigation buttons.
 *
 * @param onPlayAgain Invoked with the session's region and difficulty so the
 *   nav graph can launch a fresh quiz with the same parameters.
 * @param onHome Invoked when the user taps "Home" to return to the landing screen.
 * @param viewModel Hilt-injected [QuizResultViewModel].
 */
@Composable
fun QuizResultScreen(
    onPlayAgain: (region: String, difficulty: String) -> Unit,
    onHome: () -> Unit,
    viewModel: QuizResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val gradient = Brush.verticalGradient(
        colors = listOf(GradientMint, GradientTeal, GradientCyan, GradientSky),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        when (val state = uiState) {
            is QuizResultUiState.Loading -> ResultLoadingContent()
            is QuizResultUiState.Error -> ResultErrorContent(message = state.message, onHome = onHome)
            is QuizResultUiState.Success -> ResultSuccessContent(
                result = state.result,
                onPlayAgain = { onPlayAgain(state.result.region ?: REGION_MIXED, state.result.difficulty) },
                onHome = onHome,
            )
        }
    }
}

// ── Loading / Error ───────────────────────────────────────────────────────────

@Composable
private fun ResultLoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryGreen)
    }
}

@Composable
private fun ResultErrorContent(message: String, onHome: () -> Unit) {
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
            onClick = onHome,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
        ) {
            Text("Go Home")
        }
    }
}

// ── Success layout ────────────────────────────────────────────────────────────

@Composable
private fun ResultSuccessContent(
    result: QuizResult,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
    ) {
        item { Spacer(Modifier.height(statusBarPadding + 24.dp)) }

        // ── Hero: performance message + score ring ────────────────────────
        item {
            HeroCard(result = result)
        }

        // ── Stats row ─────────────────────────────────────────────────────
        item {
            StatsRow(result = result)
        }

        // ── Per-question breakdown ────────────────────────────────────────
        item {
            SectionHeader(text = "Answer Breakdown")
        }

        items(result.answers) { answer ->
            AnswerBreakdownRow(answer = answer)
        }

        // ── CTA buttons ───────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(4.dp))
            CtaButtons(onPlayAgain = onPlayAgain, onHome = onHome)
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Hero card ─────────────────────────────────────────────────────────────────

/**
 * Glass card containing the performance emoji + message and the circular score ring.
 *
 * @param result Completed quiz data used to derive the score and message.
 */
@Composable
private fun HeroCard(result: QuizResult) {
    GlassCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Performance message
            val (emoji, message) = performanceMessage(result.correctAnswers, result.totalQuestions)
            Text(emoji, fontSize = 48.sp)
            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall,
                color = TextHeading,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )

            // Score ring
            ScoreRing(
                correct = result.correctAnswers,
                total = result.totalQuestions,
                modifier = Modifier.size(RING_SIZE),
            )

            Text(
                text = "${result.score} pts",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Circular progress ring drawn on a [Canvas] showing the score fraction.
 *
 * The gray track represents the full circle; the green arc fills in proportion
 * to [correct] / [total].
 *
 * @param correct Number of correct answers.
 * @param total Total questions.
 */
@Composable
private fun ScoreRing(correct: Int, total: Int, modifier: Modifier = Modifier) {
    val fraction = if (total == 0) 0f else correct.toFloat() / total
    val trackColor = CardSurface
    val arcColor = SuccessGreen
    val ringStrokePx: Dp = RING_STROKE

    Box(
        modifier = modifier.drawBehind {
            val strokePx = ringStrokePx.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Score arc
            if (fraction > 0f) {
                drawArc(
                    color = arcColor,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$correct",
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextHeading,
            )
            Text(
                text = "/ $total",
                fontSize = 18.sp,
                color = TextBody,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── Stats row ─────────────────────────────────────────────────────────────────

/**
 * Row of two stat mini-cards showing accuracy percentage and quiz duration.
 *
 * @param result Source of accuracy and duration data.
 */
@Composable
private fun StatsRow(result: QuizResult) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatMiniCard(
            label = "Accuracy",
            value = "${result.accuracyPercent}%",
            modifier = Modifier.weight(1f),
        )
        StatMiniCard(
            label = "Time",
            value = result.durationMs.formatDuration(),
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Small glass card showing one stat value above its label.
 *
 * @param label Descriptor shown below the value.
 * @param value Formatted stat string (e.g. `"80%"`, `"02:34"`).
 */
@Composable
private fun StatMiniCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(STAT_CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = TextHeading,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
    }
}

// ── Per-question breakdown ────────────────────────────────────────────────────

/**
 * Single row in the answer breakdown list.
 *
 * Shows the question number, a type label, the user's answer, and a ✅/❌ icon.
 * If the answer was wrong, the correct answer is shown below in a muted colour.
 *
 * @param answer Summary data for one answered question.
 */
@Composable
private fun AnswerBreakdownRow(answer: QuizAnswerSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(STAT_CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Question number badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (answer.isCorrect) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${answer.questionNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (answer.isCorrect) SuccessGreen else ErrorRed,
                )
            }

            // Question label + answers
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = answer.questionType.toQuestionLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
                Text(
                    text = answer.userAnswer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (answer.isCorrect) TextHeading else ErrorRed,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!answer.isCorrect) {
                    Text(
                        text = "✓ ${answer.correctAnswer}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Correct / incorrect icon
            Text(
                text = if (answer.isCorrect) "✅" else "❌",
                fontSize = 20.sp,
            )
        }
    }
}

// ── CTA buttons ───────────────────────────────────────────────────────────────

/**
 * "Play Again" (gradient) and "Home" (outlined) button pair.
 *
 * @param onPlayAgain Called when the user taps "Play Again".
 * @param onHome Called when the user taps "Home".
 */
@Composable
private fun CtaButtons(onPlayAgain: () -> Unit, onHome: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Play Again — gradient fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(CtaGradientStart, CtaGradientEnd)))
                .clickable(onClick = onPlayAgain),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Play Again",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }

        // Home — outlined
        OutlinedButton(
            onClick = onHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, PrimaryGreen),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
        ) {
            Text(
                text = "Home",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

/**
 * Reusable glassmorphism card container.
 *
 * @param content Composable content placed inside the card.
 */
@Composable
private fun GlassCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = { content() },
    )
}

/**
 * Bold section header label.
 *
 * @param text Section title string.
 */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = TextHeading,
        fontWeight = FontWeight.Bold,
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Returns an emoji and a performance message based on the score.
 *
 * | Correct answers | Emoji | Message           |
 * |-----------------|-------|-------------------|
 * | 10              | 🏆    | Perfect!          |
 * | 8–9             | 🌟    | Great job!        |
 * | 6–7             | 👍    | Good effort!      |
 * | 0–5             | 💪    | Keep practicing!  |
 */
private fun performanceMessage(correct: Int, total: Int): Pair<String, String> = when {
    total == 0 -> "🤔" to "No results yet"
    correct == total -> "🏆" to "Perfect!"
    correct >= total * GREAT_THRESHOLD -> "🌟" to "Great job!"
    correct >= total * GOOD_THRESHOLD -> "👍" to "Good effort!"
    else -> "💪" to "Keep practicing!"
}

/**
 * Converts a [QuestionType] constant into a human-readable label for the
 * breakdown list.
 */
private fun String.toQuestionLabel(): String = when (this) {
    "capital" -> "What is the capital?"
    "flag" -> "Which country's flag?"
    "population_compare" -> "Larger population?"
    "reverse_capital" -> "Capital belongs to?"
    "region" -> "Which continent?"
    "landlocked_tf" -> "True or False: landlocked?"
    else -> replace("_", " ").replaceFirstChar { it.uppercase() }
}

/**
 * Formats a duration in milliseconds as `mm:ss`.
 *
 * @return String in the format `"02:34"`.
 */
private fun Long.formatDuration(): String {
    val totalSeconds = this / MILLIS_PER_SECOND
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "%02d:%02d".format(minutes, seconds)
}

private const val GREAT_THRESHOLD = 0.8f
private const val GOOD_THRESHOLD = 0.6f
private const val MILLIS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val REGION_MIXED = "mixed"
