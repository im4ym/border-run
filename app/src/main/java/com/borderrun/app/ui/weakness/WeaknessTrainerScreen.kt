package com.borderrun.app.ui.weakness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borderrun.app.ui.quiz.QuizActiveContent
import com.borderrun.app.ui.quiz.QuizUiState
import com.borderrun.app.ui.theme.ErrorRed
import com.borderrun.app.ui.theme.DarkGradientStop1
import com.borderrun.app.ui.theme.DarkGradientStop2
import com.borderrun.app.ui.theme.DarkGradientStop3
import com.borderrun.app.ui.theme.DarkGradientStop4
import com.borderrun.app.ui.theme.GradientCyan
import com.borderrun.app.ui.theme.GradientMint
import com.borderrun.app.ui.theme.GradientSky
import com.borderrun.app.ui.theme.GradientTeal
import com.borderrun.app.ui.theme.LocalIsDarkTheme
import com.borderrun.app.ui.theme.PrimaryGreen

/**
 * Root composable for the Smart Weakness Trainer screen.
 *
 * Renders the standard quiz UI (re-using [QuizActiveContent]) with the title
 * set to "Training: [region]" so the user knows which region they are drilling.
 *
 * Navigation into this screen requires the target region to have been identified
 * by [com.borderrun.app.domain.usecase.GetWeaknessDataUseCase] (≥ 20 recorded
 * answers). The region is passed as a navigation argument by the NavGraph.
 *
 * On completion, the session is recorded in Room exactly like a classic quiz
 * session, feeding the accuracy stats that drive the weakness detection.
 *
 * @param onNavigateToResult Invoked with the completed session ID when the last
 *   question is answered.
 * @param onNavigateBack Invoked when the user taps the back arrow.
 * @param viewModel Hilt-injected [WeaknessTrainerViewModel].
 */
@Composable
fun WeaknessTrainerScreen(
    onNavigateToResult: (sessionId: Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: WeaknessTrainerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate to result when the training session is complete.
    val completedState = uiState as? QuizUiState.Complete
    LaunchedEffect(completedState) {
        if (completedState != null) onNavigateToResult(completedState.sessionId)
    }

    val isDark = LocalIsDarkTheme.current
    val gradient = Brush.verticalGradient(
        colors = if (isDark) listOf(DarkGradientStop1, DarkGradientStop2, DarkGradientStop3, DarkGradientStop4)
                 else listOf(GradientMint, GradientTeal, GradientCyan, GradientSky),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        when (val state = uiState) {
            is QuizUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            is QuizUiState.Error -> {
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
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = ErrorRed,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    ) {
                        Text("Go Back")
                    }
                }
            }
            is QuizUiState.Active -> QuizActiveContent(
                state = state,
                onNavigateBack = onNavigateBack,
                onAnswerSelected = viewModel::onAnswerSelected,
                onNextQuestion = viewModel::onNextQuestion,
                titleOverride = "Training: ${viewModel.region}",
            )
            is QuizUiState.Complete -> {
                // Brief loading flash while navigation fires.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
        }
    }
}
