package com.borderrun.app.domain

import com.borderrun.app.domain.model.Country
import com.borderrun.app.domain.model.QuestionType
import com.borderrun.app.domain.model.QuizQuestion
import javax.inject.Inject

/**
 * Generates [QuizQuestion] instances from a pool of [Country] domain objects.
 *
 * Six question types are supported across three difficulty tiers. Type slots
 * are shuffled for each quiz to ensure variety. Each generator function returns
 * `null` when a country lacks the required data (e.g. blank capital) — the
 * generator retries with the next country.
 *
 * All methods are pure Kotlin with no Android dependencies, enabling full unit
 * testing without an Android device or emulator.
 */
class QuestionGenerator @Inject constructor() {

    /**
     * Generates [count] questions from [allCountries] filtered to [region] at
     * the given [difficulty].
     *
     * @param allCountries Full list of countries from Room.
     * @param region Region to quiz on. `"mixed"` or `"daily"` uses the full list.
     * @param difficulty One of `"easy"`, `"medium"`, `"hard"`.
     * @param count Target number of questions. Fewer may be returned if the
     *   pool is too small.
     * @return List of generated questions, up to [count] items.
     */
    fun generate(
        allCountries: List<Country>,
        region: String,
        difficulty: String,
        count: Int = DEFAULT_QUIZ_LENGTH,
    ): List<QuizQuestion> {
        val pool = buildPool(allCountries, region)
        if (pool.size < MIN_COUNTRIES_FOR_QUIZ) return emptyList()

        val typeSlots = buildTypeSlots(difficulty, count)
        val questions = mutableListOf<QuizQuestion>()
        val shuffledPool = pool.shuffled()
        var countryIndex = 0

        for (type in typeSlots) {
            if (questions.size >= count) break
            // Attempt each country in order until a valid question is produced.
            var attempts = 0
            while (attempts < shuffledPool.size) {
                val country = shuffledPool[countryIndex % shuffledPool.size]
                countryIndex++
                attempts++
                val question = when (type) {
                    QuestionType.CAPITAL -> makeCapitalQuestion(country, pool, difficulty)
                    QuestionType.FLAG -> makeFlagQuestion(country, pool, difficulty)
                    QuestionType.POPULATION_COMPARE -> makePopulationCompareQuestion(pool, difficulty)
                    QuestionType.REVERSE_CAPITAL -> makeReverseCapitalQuestion(country, pool, difficulty)
                    QuestionType.REGION -> makeRegionQuestion(country, difficulty)
                    QuestionType.LANDLOCKED_TF -> makeLandlockedQuestion(country, difficulty)
                    else -> null
                }
                if (question != null) {
                    questions.add(question)
                    break
                }
            }
        }

        return questions
    }

    // ── Pool building ─────────────────────────────────────────────────────────

    /**
     * Returns the subset of [allCountries] relevant to [region].
     *
     * `"mixed"` and `"daily"` return the full list.
     */
    private fun buildPool(allCountries: List<Country>, region: String): List<Country> =
        if (region == REGION_MIXED || region == REGION_DAILY) allCountries
        else allCountries.filter { it.region == region }

    /**
     * Returns a shuffled list of [count] question-type slots for [difficulty].
     *
     * Easy tier uses 5 types; medium and hard add population comparison.
     */
    private fun buildTypeSlots(difficulty: String, count: Int): List<String> {
        val available = when (difficulty) {
            DIFFICULTY_EASY -> listOf(
                QuestionType.CAPITAL,
                QuestionType.FLAG,
                QuestionType.REVERSE_CAPITAL,
                QuestionType.REGION,
                QuestionType.LANDLOCKED_TF,
            )
            else -> listOf(
                QuestionType.CAPITAL,
                QuestionType.FLAG,
                QuestionType.POPULATION_COMPARE,
                QuestionType.REVERSE_CAPITAL,
                QuestionType.REGION,
                QuestionType.LANDLOCKED_TF,
            )
        }
        val slots = mutableListOf<String>()
        while (slots.size < count) slots.addAll(available.shuffled())
        return slots.shuffled().take(count)
    }

    // ── Question type generators ──────────────────────────────────────────────

    /**
     * "What is the capital of [country]?" with four capital options.
     *
     * Returns `null` if [country] has no capital or if the pool has fewer than
     * four countries with capitals (needed for distractors).
     */
    private fun makeCapitalQuestion(
        country: Country,
        pool: List<Country>,
        difficulty: String,
    ): QuizQuestion.MultipleChoice? {
        if (country.capital.isBlank()) return null
        val distractors = pool
            .filter { it.id != country.id && it.capital.isNotBlank() }
            .shuffled()
            .take(DISTRACTOR_COUNT)
            .map { it.capital }
        if (distractors.size < DISTRACTOR_COUNT) return null
        return QuizQuestion.MultipleChoice(
            questionText = "What is the capital of ${country.name}?",
            correctAnswer = country.capital,
            options = (listOf(country.capital) + distractors).shuffled(),
            region = country.region,
            difficulty = difficulty,
            explanationText = "${country.capital} is the capital city of ${country.name}.",
            questionType = QuestionType.CAPITAL,
            primaryCountryId = country.id,
        )
    }

    /**
     * "Which country has this flag?" — shows a flag image with four country name options.
     *
     * Returns `null` if the pool has fewer than four countries.
     */
    private fun makeFlagQuestion(
        country: Country,
        pool: List<Country>,
        difficulty: String,
    ): QuizQuestion.MultipleChoice? {
        val distractors = pool
            .filter { it.id != country.id }
            .shuffled()
            .take(DISTRACTOR_COUNT)
            .map { it.name }
        if (distractors.size < DISTRACTOR_COUNT) return null
        return QuizQuestion.MultipleChoice(
            questionText = "Which country's flag is this?",
            correctAnswer = country.name,
            options = (listOf(country.name) + distractors).shuffled(),
            flagUrl = country.flagUrl,
            region = country.region,
            difficulty = difficulty,
            explanationText = "This is the flag of ${country.name}.",
            questionType = QuestionType.FLAG,
            primaryCountryId = country.id,
        )
    }

    /**
     * "Which country has a larger population?" — CompareTwo format.
     *
     * Returns `null` if the pool has fewer than two countries or if both
     * selected countries share an identical population (to avoid a tie).
     */
    private fun makePopulationCompareQuestion(
        pool: List<Country>,
        difficulty: String,
    ): QuizQuestion.CompareTwo? {
        if (pool.size < 2) return null
        val (a, b) = pool.shuffled().take(2)
        if (a.population == b.population) return null
        val winner = if (a.population > b.population) a else b
        val loser = if (a.population > b.population) b else a
        return QuizQuestion.CompareTwo(
            questionText = "Which country has a larger population?",
            correctAnswer = winner.name,
            countryA = a,
            countryB = b,
            region = winner.region,
            difficulty = difficulty,
            explanationText = "${winner.name} (${winner.population.formatPopulation()}) " +
                "has more people than ${loser.name} (${loser.population.formatPopulation()}).",
            questionType = QuestionType.POPULATION_COMPARE,
            primaryCountryId = winner.id,
        )
    }

    /**
     * "[Capital] is the capital of which country?" with four country name options.
     *
     * Returns `null` if [country] has no capital or the pool is too small.
     */
    private fun makeReverseCapitalQuestion(
        country: Country,
        pool: List<Country>,
        difficulty: String,
    ): QuizQuestion.MultipleChoice? {
        if (country.capital.isBlank()) return null
        val distractors = pool
            .filter { it.id != country.id }
            .shuffled()
            .take(DISTRACTOR_COUNT)
            .map { it.name }
        if (distractors.size < DISTRACTOR_COUNT) return null
        return QuizQuestion.MultipleChoice(
            questionText = "${country.capital} is the capital of which country?",
            correctAnswer = country.name,
            options = (listOf(country.name) + distractors).shuffled(),
            region = country.region,
            difficulty = difficulty,
            explanationText = "${country.capital} is the capital city of ${country.name}.",
            questionType = QuestionType.REVERSE_CAPITAL,
            primaryCountryId = country.id,
        )
    }

    /**
     * "Which continent is [country] in?" with four region options.
     *
     * Always returns a valid question as long as [QUIZ_REGIONS] contains at
     * least four entries.
     */
    private fun makeRegionQuestion(
        country: Country,
        difficulty: String,
    ): QuizQuestion.MultipleChoice {
        val correctRegion = country.region
        val distractors = QUIZ_REGIONS
            .filter { it != correctRegion }
            .shuffled()
            .take(DISTRACTOR_COUNT)
        val options = (listOf(correctRegion) + distractors).shuffled()
        return QuizQuestion.MultipleChoice(
            questionText = "Which continent is ${country.name} in?",
            correctAnswer = correctRegion,
            options = options,
            region = country.region,
            difficulty = difficulty,
            explanationText = "${country.name} is located in ${country.region}.",
            questionType = QuestionType.REGION,
            primaryCountryId = country.id,
        )
    }

    /**
     * "True or False: [country] is landlocked."
     *
     * Always returns a valid question for any country.
     */
    private fun makeLandlockedQuestion(
        country: Country,
        difficulty: String,
    ): QuizQuestion.TrueFalse {
        val isLandlocked = country.isLandlocked
        val coastDetail = if (isLandlocked) "has no coastline" else "has a coastline"
        return QuizQuestion.TrueFalse(
            questionText = "True or False: ${country.name} is a landlocked country.",
            correctAnswer = if (isLandlocked) ANSWER_TRUE else ANSWER_FALSE,
            region = country.region,
            difficulty = difficulty,
            explanationText = "${country.name} $coastDetail.",
            questionType = QuestionType.LANDLOCKED_TF,
            primaryCountryId = country.id,
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Formats a [Long] population count into a compact human-readable string.
     *
     * | Range     | Format | Example |
     * |-----------|--------|---------|
     * | ≥ 1B      | XB     | 1.4B    |
     * | ≥ 1M      | XM     | 97M     |
     * | ≥ 1K      | XK     | 800K    |
     * | otherwise | raw    | 500     |
     */
    private fun Long.formatPopulation(): String = when {
        this >= 1_000_000_000L -> "${"%.1f".format(this / 1_000_000_000.0)}B"
        this >= 1_000_000L -> "${this / 1_000_000}M"
        this >= 1_000L -> "${this / 1_000}K"
        else -> toString()
    }

    companion object {
        /** Default number of questions per quiz session. */
        const val DEFAULT_QUIZ_LENGTH = 10

        /** Minimum pool size required to generate any question type. */
        const val MIN_COUNTRIES_FOR_QUIZ = 4

        /** Number of wrong-answer distractors per multiple-choice question. */
        private const val DISTRACTOR_COUNT = 3

        /** Region value used for mixed / no-filter quizzes. */
        private const val REGION_MIXED = "mixed"

        /** Region value used for daily-challenge quizzes. */
        private const val REGION_DAILY = "daily"

        private const val DIFFICULTY_EASY = "easy"

        /** True/False answer string for landlocked questions. */
        const val ANSWER_TRUE = "True"

        /** True/False answer string for landlocked questions. */
        const val ANSWER_FALSE = "False"

        /**
         * Standard set of quiz regions used as multiple-choice options for
         * region questions. Fixed to ensure consistent 4-option sets.
         */
        private val QUIZ_REGIONS = listOf("Africa", "Americas", "Asia", "Europe", "Oceania")
    }
}
