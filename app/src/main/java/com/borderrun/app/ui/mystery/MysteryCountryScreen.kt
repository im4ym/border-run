package com.borderrun.app.ui.mystery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.borderrun.app.ui.theme.CtaGradientEnd
import com.borderrun.app.ui.theme.CtaGradientStart
import com.borderrun.app.ui.theme.DarkGradientStop1
import com.borderrun.app.ui.theme.DarkGradientStop2
import com.borderrun.app.ui.theme.DarkGradientStop3
import com.borderrun.app.ui.theme.DarkGradientStop4
import com.borderrun.app.ui.theme.ErrorRed
import com.borderrun.app.ui.theme.GradientCyan
import com.borderrun.app.ui.theme.GradientMint
import com.borderrun.app.ui.theme.GradientSky
import com.borderrun.app.ui.theme.GradientTeal
import com.borderrun.app.ui.theme.LocalIsDarkTheme
import com.borderrun.app.ui.theme.PrimaryGreen
import com.borderrun.app.ui.theme.SuccessGreen

// ── Layout constants ──────────────────────────────────────────────────────────

private val CARD_RADIUS = 22.dp
private val CARD_PADDING = 20.dp
private val SECTION_SPACING = 12.dp
private val BADGE_SIZE = 28.dp

/**
 * Root composable for the Daily Mystery Country screen.
 *
 * Shows a "guess the country" puzzle with up to [MAX_MYSTERY_CLUES] progressive
 * clues. Each clue is revealed one at a time. The user may submit a text guess
 * at any point; wrong guesses increment the attempt counter but do not auto-
 * reveal clues. Once all 5 clues are shown without a correct answer, the user
 * can tap "Give Up" to see the answer.
 *
 * Daily selection is deterministic (seeded by today's date) so every user sees
 * the same mystery. Progress is persisted to Room across app restarts.
 *
 * @param onBack Navigates back to the Home screen.
 * @param viewModel Hilt-injected [MysteryCountryViewModel].
 */
@Composable
fun MysteryCountryScreen(
    onBack: () -> Unit,
    viewModel: MysteryCountryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

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
            is MysteryUiState.Loading -> MysteryLoadingContent()
            is MysteryUiState.Error -> MysteryErrorContent(message = state.message, onBack = onBack)
            is MysteryUiState.Active -> MysteryActiveContent(
                state = state,
                onBack = onBack,
                onRevealNextClue = viewModel::revealNextClue,
                onSubmitGuess = viewModel::submitGuess,
                onGiveUp = viewModel::giveUp,
                onPlayAgain = viewModel::resetMystery,
            )
        }
    }
}

// ── Loading / Error ───────────────────────────────────────────────────────────

@Composable
private fun MysteryLoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryGreen)
    }
}

@Composable
private fun MysteryErrorContent(message: String, onBack: () -> Unit) {
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
        GradientButton(text = "Go Back", onClick = onBack)
    }
}

// ── Active content ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MysteryActiveContent(
    state: MysteryUiState.Active,
    onBack: () -> Unit,
    onRevealNextClue: () -> Unit,
    onSubmitGuess: (String) -> Unit,
    onGiveUp: () -> Unit,
    onPlayAgain: () -> Unit,
) {
    var guessText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mystery Country",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    Text(
                        text = if (state.attempts > 0) "${state.attempts} attempt${if (state.attempts == 1) "" else "s"}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            // Header
            MysteryHeaderCard()

            // Clue cards (revealed + locked)
            for (i in 0 until MAX_MYSTERY_CLUES) {
                if (i < state.cluesRevealed) {
                    RevealedClueCard(index = i, text = state.clues.getOrElse(i) { "" })
                } else {
                    LockedClueCard(index = i)
                }
            }

            Spacer(Modifier.height(4.dp))

            // Result card (shown when puzzle is complete)
            if (state.isComplete) {
                MysteryResultCard(
                    solved = state.solved,
                    attempts = state.attempts,
                    countryName = state.countryName,
                    countryFlagUrl = state.countryFlagUrl,
                )
                GradientButton(text = "Play Again", onClick = onPlayAgain)
            } else {
                // Reveal next clue button
                if (state.canRevealMore) {
                    GradientButton(
                        text = "Reveal Next Clue (${state.cluesRevealed}/$MAX_MYSTERY_CLUES)",
                        onClick = onRevealNextClue,
                    )
                }

                // Guess input
                OutlinedTextField(
                    value = guessText,
                    onValueChange = { guessText = it },
                    label = { Text("Type your guess…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (guessText.isNotBlank()) {
                                onSubmitGuess(guessText.trim())
                                guessText = ""
                            }
                        },
                    ),
                )

                // Wrong-guess feedback
                if (state.attempts > 0) {
                    Text(
                        text = "Incorrect! Try again or reveal another clue.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }

                // Submit button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (guessText.isNotBlank())
                                Brush.horizontalGradient(listOf(CtaGradientStart, CtaGradientEnd))
                            else
                                Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline)),
                        )
                        .clickable(enabled = guessText.isNotBlank()) {
                            onSubmitGuess(guessText.trim())
                            guessText = ""
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Submit Guess",
                        color = if (guessText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }

                // Give Up button (only when all clues shown)
                if (state.canGiveUp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(onClick = onGiveUp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Give Up — Reveal Answer",
                            color = ErrorRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Mystery header card ───────────────────────────────────────────────────────

@Composable
private fun MysteryHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(CARD_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("🕵️", fontSize = 40.sp)
            Text(
                text = "Guess Today's Country",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Clues are revealed one at a time. Use as few as possible!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Clue cards ────────────────────────────────────────────────────────────────

@Composable
private fun RevealedClueCard(index: Int, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, PrimaryGreen.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(BADGE_SIZE)
                    .clip(CircleShape)
                    .background(PrimaryGreen),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${index + 1}",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LockedClueCard(index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(BADGE_SIZE)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${index + 1}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
            Text(
                text = "🔒  Clue ${index + 1} — not yet revealed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ── Result card ───────────────────────────────────────────────────────────────

@Composable
private fun MysteryResultCard(
    solved: Boolean,
    attempts: Int,
    countryName: String,
    countryFlagUrl: String,
) {
    val icon = if (solved) "🎉" else "😔"
    val headline = if (solved) "You got it!" else "Better luck tomorrow!"
    val headlineColor = if (solved) SuccessGreen else ErrorRed
    val subtext = if (solved) {
        "Solved in ${if (attempts == 0) "1 attempt" else "$attempts attempt${if (attempts == 1) "" else "s"}"}"
    } else {
        "The mystery country was:"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (solved) SuccessGreen.copy(alpha = 0.5f) else ErrorRed.copy(alpha = 0.3f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(CARD_PADDING),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(icon, fontSize = 40.sp)
            Text(
                text = headline,
                style = MaterialTheme.typography.titleLarge,
                color = headlineColor,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (countryFlagUrl.isNotEmpty()) {
                AsyncImage(
                    model = countryFlagUrl,
                    contentDescription = "$countryName flag",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                )
            }
            Text(
                text = countryName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Gradient button ───────────────────────────────────────────────────────────

@Composable
private fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(CtaGradientStart, CtaGradientEnd)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
