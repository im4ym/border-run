package com.borderrun.app.ui.mystery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borderrun.app.data.local.dao.DailyMysteryDao
import com.borderrun.app.data.local.entity.DailyMysteryEntity
import com.borderrun.app.domain.model.Country
import com.borderrun.app.domain.repository.CountryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Random
import javax.inject.Inject
import kotlin.math.abs

/**
 * ViewModel for the Daily Mystery Country screen.
 *
 * On creation, [loadMystery] checks Room for today's mystery. If none exists, it
 * selects a country deterministically seeded by today's UTC midnight timestamp and
 * inserts a new [DailyMysteryEntity]. Progress (clues revealed, attempts, solved
 * state) is persisted to Room after every action so the puzzle survives app
 * restarts.
 *
 * The puzzle has five progressive clues:
 * 1. Region
 * 2. Population range
 * 3. Landlocked status
 * 4. First letter of the capital
 * 5. Number of land borders
 *
 * @property countryRepository Provides the cached country list and look-up by ID.
 * @property dailyMysteryDao Persists and retrieves the daily mystery entity.
 */
@HiltViewModel
class MysteryCountryViewModel @Inject constructor(
    private val countryRepository: CountryRepository,
    private val dailyMysteryDao: DailyMysteryDao,
) : ViewModel() {

    /**
     * Unix timestamp (ms) for today's midnight in UTC — used as the Room lookup
     * key and as the seed for the deterministic country selection.
     */
    private val todayMidnightMs: Long =
        LocalDate.now(ZoneOffset.UTC)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

    private val _uiState = MutableStateFlow<MysteryUiState>(MysteryUiState.Loading)

    /** Observed by [MysteryCountryScreen] to drive the UI. */
    val uiState: StateFlow<MysteryUiState> = _uiState.asStateFlow()

    // ── Snapshot ──────────────────────────────────────────────────────────────

    /** Current Room entity — updated after every action and persisted via upsert. */
    private var entity: DailyMysteryEntity? = null

    /** Domain model for the answer country — only used to build clues and show result. */
    private var country: Country? = null

    /** Pre-built list of all five clues. Generated once per puzzle. */
    private var allClues: List<String> = emptyList()

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        viewModelScope.launch { loadMystery() }
    }

    // ── Loading ───────────────────────────────────────────────────────────────

    private suspend fun loadMystery() {
        try {
            val existing = dailyMysteryDao.getMysteryForDay(todayMidnightMs).first()
            if (existing != null) {
                val c = countryRepository.getCountryById(existing.countryId).first()
                if (c == null) {
                    _uiState.value = MysteryUiState.Error("Country data unavailable. Please sync.")
                    return
                }
                // Ensure at least 1 clue is revealed (guard against legacy 0 rows)
                entity = if (existing.cluesRevealed < 1) {
                    val fixed = existing.copy(cluesRevealed = 1)
                    dailyMysteryDao.upsertMystery(fixed)
                    fixed
                } else {
                    existing
                }
                country = c
                allClues = buildClues(c)
                emitState()
            } else {
                // No mystery for today — pick one seeded by the date.
                val countries = countryRepository.getAllCountries().first()
                if (countries.isEmpty()) {
                    _uiState.value = MysteryUiState.Error(
                        "No country data available. Please check your connection.",
                    )
                    return
                }
                val idx = abs(Random(todayMidnightMs).nextInt()) % countries.size
                val c = countries[idx]
                val newEntity = DailyMysteryEntity(
                    date = todayMidnightMs,
                    countryId = c.id,
                    cluesRevealed = 1,
                )
                val insertedId = dailyMysteryDao.insertMystery(newEntity).toInt()
                entity = newEntity.copy(id = insertedId)
                country = c
                allClues = buildClues(c)
                emitState()
            }
        } catch (e: Exception) {
            _uiState.value = MysteryUiState.Error("Failed to load mystery: ${e.message}")
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Reveals the next clue and persists the updated [DailyMysteryEntity].
     *
     * No-ops when all clues are already shown or the puzzle is complete.
     */
    fun revealNextClue() {
        val e = entity ?: return
        if (e.solved || e.cluesRevealed >= MAX_MYSTERY_CLUES) return
        val updated = e.copy(cluesRevealed = e.cluesRevealed + 1)
        entity = updated
        save(updated)
        emitState()
    }

    /**
     * Submits a guess and persists the result.
     *
     * Accepted answers: case-insensitive match against the country's common name
     * or official name (trimmed). No-ops if the puzzle is already solved.
     *
     * @param guess The user's text input.
     */
    fun submitGuess(guess: String) {
        val e = entity ?: return
        val c = country ?: return
        if (e.solved) return
        val trimmed = guess.trim()
        val isCorrect = trimmed.equals(c.name, ignoreCase = true) ||
            trimmed.equals(c.officialName, ignoreCase = true)
        val updated = e.copy(
            attempts = e.attempts + 1,
            solved = isCorrect,
            solvedAt = if (isCorrect) System.currentTimeMillis() else e.solvedAt,
        )
        entity = updated
        save(updated)
        emitState()
    }

    /**
     * Marks the puzzle as gave-up and reveals the answer.
     *
     * Only callable when all [MAX_MYSTERY_CLUES] clues are shown and the puzzle
     * is not yet solved.
     */
    fun giveUp() {
        val e = entity ?: return
        if (e.solved || e.cluesRevealed < MAX_MYSTERY_CLUES) return
        // Mark gaveUp by setting attempts to a sentinel that the state builder reads.
        // We reuse the entity as-is (solved=false, all clues revealed) — the
        // [MysteryUiState.Active.gaveUp] flag is derived from those values.
        // Force a re-emit to update the UI with the revealed answer.
        emitState(forceGiveUp = true)
    }

    /**
     * Resets today's puzzle so the user can play it again from scratch.
     *
     * Deletes the [DailyMysteryEntity] row for today from Room, clears all
     * in-memory state, then re-runs [loadMystery] which will generate and
     * insert a fresh entity (same country, deterministically seeded by date).
     */
    fun resetMystery() {
        viewModelScope.launch {
            dailyMysteryDao.deleteMysteryForDay(todayMidnightMs)
            entity = null
            country = null
            allClues = emptyList()
            _uiState.value = MysteryUiState.Loading
            loadMystery()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun save(updated: DailyMysteryEntity) {
        viewModelScope.launch { dailyMysteryDao.upsertMystery(updated) }
    }

    private fun emitState(forceGiveUp: Boolean = false) {
        val e = entity ?: return
        val c = country ?: return
        val gaveUp = forceGiveUp || (e.cluesRevealed >= MAX_MYSTERY_CLUES && !e.solved)
        val isComplete = e.solved || gaveUp
        _uiState.value = MysteryUiState.Active(
            clues = allClues,
            cluesRevealed = e.cluesRevealed,
            attempts = e.attempts,
            solved = e.solved,
            gaveUp = gaveUp,
            countryName = if (isComplete) c.name else "",
            countryFlagUrl = if (isComplete) c.flagUrl else "",
        )
    }

    /**
     * Builds the ordered list of five progressive clues for the given [country].
     *
     * Clue ordering (easiest → hardest):
     * 1. Region
     * 2. Population range
     * 3. Landlocked status
     * 4. First letter of capital
     * 5. Number of land borders
     */
    private fun buildClues(country: Country): List<String> {
        val capitalLetter = country.capital.firstOrNull()?.uppercase() ?: "?"
        val borderCount = country.borders.size
        val borderWord = if (borderCount == 1) "country" else "countries"
        return listOf(
            "This country is located in ${country.region}.",
            "Population: ${populationRange(country.population)}.",
            if (country.isLandlocked) "This country is landlocked (no sea access)."
            else "This country has access to the sea.",
            "The capital city starts with the letter '$capitalLetter'.",
            "This country shares borders with $borderCount $borderWord.",
        )
    }

    private fun populationRange(pop: Long): String = when {
        pop < 1_000_000L -> "under 1 million"
        pop < 10_000_000L -> "1–10 million"
        pop < 50_000_000L -> "10–50 million"
        pop < 100_000_000L -> "50–100 million"
        pop < 500_000_000L -> "100–500 million"
        else -> "over 500 million"
    }
}
