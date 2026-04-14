package com.borderrun.app.ui.mystery

/** Maximum number of progressive clues available per mystery puzzle. */
const val MAX_MYSTERY_CLUES = 5

/**
 * Sealed UI state hierarchy for the Daily Mystery Country screen.
 *
 * Transitions: [Loading] → [Active] (mystery loaded) → [Active] with
 * [Active.solved] or [Active.gaveUp] set to `true` (puzzle complete).
 * [Error] is a terminal state reached when country data is unavailable.
 */
sealed class MysteryUiState {

    /** Initial state while the mystery is being loaded from Room. */
    data object Loading : MysteryUiState()

    /**
     * Terminal error state — country cache is empty or the mystery entity
     * could not be loaded.
     *
     * @property message Human-readable description shown to the user.
     */
    data class Error(val message: String) : MysteryUiState()

    /**
     * Active puzzle state — mystery loaded and the user is playing.
     *
     * @property clues All five clues ordered by difficulty (easiest first).
     *   Always contains exactly [MAX_MYSTERY_CLUES] entries.
     * @property cluesRevealed How many clues are currently visible (1–[MAX_MYSTERY_CLUES]).
     *   Always at least 1 so the first clue is shown immediately.
     * @property attempts Total number of incorrect guess submissions so far.
     * @property solved `true` after the user submits the correct country name.
     * @property gaveUp `true` after the user taps "Give Up" (all clues revealed,
     *   not yet solved). Mutually exclusive with [solved].
     * @property countryName The answer country's common name. Only non-empty when
     *   [solved] or [gaveUp] is `true` — hidden until the puzzle is complete to
     *   prevent cheating.
     * @property countryFlagUrl PNG flag URL for the answer country. Empty while
     *   the puzzle is still active.
     */
    data class Active(
        val clues: List<String>,
        val cluesRevealed: Int,
        val attempts: Int,
        val solved: Boolean,
        val gaveUp: Boolean,
        val countryName: String,
        val countryFlagUrl: String,
    ) : MysteryUiState() {

        /** `true` when the puzzle is over (either solved or gave up). */
        val isComplete: Boolean get() = solved || gaveUp

        /** `true` when more clues can still be revealed. */
        val canRevealMore: Boolean get() = cluesRevealed < MAX_MYSTERY_CLUES && !isComplete

        /** `true` when all clues are shown and the puzzle is not yet solved. */
        val canGiveUp: Boolean get() = cluesRevealed >= MAX_MYSTERY_CLUES && !isComplete
    }
}
