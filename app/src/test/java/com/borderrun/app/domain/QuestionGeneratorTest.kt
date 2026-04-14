package com.borderrun.app.domain

import com.borderrun.app.domain.model.Country
import com.borderrun.app.domain.model.QuestionType
import com.borderrun.app.domain.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionGeneratorTest {

    private val generator = QuestionGenerator()

    // ── Test helpers ──────────────────────────────────────────────────────────

    private fun fakeCountry(
        id: String,
        name: String,
        capital: String = "${name}City",
        region: String = "Asia",
        population: Long = 1_000_000L,
        isLandlocked: Boolean = false,
    ) = Country(
        id = id,
        name = name,
        officialName = "Official $name",
        capital = capital,
        region = region,
        subregion = "Sub $region",
        flagUrl = "https://flag.example.com/$id.png",
        population = population,
        area = 100_000.0,
        languages = listOf("English"),
        currencies = listOf("Dollar"),
        borders = emptyList(),
        isLandlocked = isLandlocked,
        drivingSide = "right",
    )

    /** Ten distinct Asia countries, each with a unique capital and population. */
    private val asiaCountries = (1..10).map { i ->
        fakeCountry("AS$i", "AsiaCountry$i", "AsiaCapital$i", "Asia", population = i * 1_000_000L)
    }

    /** Five Europe countries for mixed-region tests. */
    private val europeCountries = (1..5).map { i ->
        fakeCountry("EU$i", "EuroCountry$i", "EuroCapital$i", "Europe", population = i * 2_000_000L)
    }

    private val mixedCountries = asiaCountries + europeCountries

    // ── Question count ────────────────────────────────────────────────────────

    @Test
    fun `generate returns requested number of questions`() {
        val questions = generator.generate(asiaCountries, "Asia", "medium", 5)
        assertEquals(5, questions.size)
    }

    @Test
    fun `generate returns DEFAULT_QUIZ_LENGTH when count not specified`() {
        val questions = generator.generate(asiaCountries, "Asia", "medium")
        assertEquals(QuestionGenerator.DEFAULT_QUIZ_LENGTH, questions.size)
    }

    @Test
    fun `generate returns empty list when pool is smaller than MIN_COUNTRIES_FOR_QUIZ`() {
        val tooFew = asiaCountries.take(QuestionGenerator.MIN_COUNTRIES_FOR_QUIZ - 1)
        val questions = generator.generate(tooFew, "Asia", "medium")
        assertTrue(questions.isEmpty())
    }

    // ── Correct answer in options ─────────────────────────────────────────────

    @Test
    fun `correct answer is always among options for MultipleChoice questions`() {
        val questions = generator.generate(asiaCountries, "mixed", "medium", 20)
        questions.filterIsInstance<QuizQuestion.MultipleChoice>().forEach { q ->
            assertTrue(
                "Correct answer '${q.correctAnswer}' not in options ${q.options}",
                q.correctAnswer in q.options,
            )
        }
    }

    @Test
    fun `MultipleChoice questions always have exactly four options`() {
        val questions = generator.generate(asiaCountries, "mixed", "medium", 20)
        questions.filterIsInstance<QuizQuestion.MultipleChoice>().forEach { q ->
            assertEquals(
                "Expected 4 options, got ${q.options.size} for '${q.questionText}'",
                4,
                q.options.size,
            )
        }
    }

    @Test
    fun `TrueFalse correct answer is always True or False`() {
        val questions = generator.generate(asiaCountries, "mixed", "medium", 30)
        questions.filterIsInstance<QuizQuestion.TrueFalse>().forEach { q ->
            assertTrue(
                "Expected '${QuestionGenerator.ANSWER_TRUE}' or '${QuestionGenerator.ANSWER_FALSE}'" +
                    " but got '${q.correctAnswer}'",
                q.correctAnswer == QuestionGenerator.ANSWER_TRUE ||
                    q.correctAnswer == QuestionGenerator.ANSWER_FALSE,
            )
        }
    }

    // ── Region filtering ──────────────────────────────────────────────────────

    @Test
    fun `region filtering restricts questions to specified region`() {
        val questions = generator.generate(mixedCountries, "Europe", "medium", 5)
        val europeIds = europeCountries.map { it.id }.toSet()
        questions.forEach { q ->
            assertTrue(
                "primaryCountryId '${q.primaryCountryId}' is not from Europe",
                q.primaryCountryId in europeIds,
            )
        }
    }

    @Test
    fun `mixed region uses all countries`() {
        // With 15 countries total, mixed quiz should produce 10 questions
        val questions = generator.generate(mixedCountries, "mixed", "medium")
        assertEquals(QuestionGenerator.DEFAULT_QUIZ_LENGTH, questions.size)
    }

    @Test
    fun `daily region uses all countries`() {
        val questions = generator.generate(mixedCountries, "daily", "medium")
        assertEquals(QuestionGenerator.DEFAULT_QUIZ_LENGTH, questions.size)
    }

    @Test
    fun `filtering to a region with fewer than MIN_COUNTRIES returns empty`() {
        val questions = generator.generate(mixedCountries, "Oceania", "medium")
        assertTrue(questions.isEmpty())
    }

    // ── Question types ────────────────────────────────────────────────────────

    @Test
    fun `easy difficulty does not include POPULATION_COMPARE`() {
        // Generate many questions to cover the type distribution
        val questions = (1..5).flatMap {
            generator.generate(asiaCountries, "mixed", "easy", 10)
        }
        val types = questions.map { it.questionType }.toSet()
        assertFalse(
            "POPULATION_COMPARE must not appear in easy difficulty",
            QuestionType.POPULATION_COMPARE in types,
        )
    }

    @Test
    fun `medium difficulty includes POPULATION_COMPARE over many questions`() {
        val questions = (1..10).flatMap {
            generator.generate(asiaCountries, "mixed", "medium", 10)
        }
        val types = questions.map { it.questionType }.toSet()
        assertTrue(
            "POPULATION_COMPARE should appear at least once across 100 medium questions",
            QuestionType.POPULATION_COMPARE in types,
        )
    }

    @Test
    fun `all standard question types appear in medium difficulty over many runs`() {
        val expectedTypes = setOf(
            QuestionType.CAPITAL,
            QuestionType.FLAG,
            QuestionType.REVERSE_CAPITAL,
            QuestionType.REGION,
            QuestionType.LANDLOCKED_TF,
        )
        val questions = (1..10).flatMap {
            generator.generate(asiaCountries, "mixed", "medium", 10)
        }
        val actualTypes = questions.map { it.questionType }.toSet()
        expectedTypes.forEach { type ->
            assertTrue(
                "Question type '$type' not found in ${actualTypes}",
                type in actualTypes,
            )
        }
    }
}
