package com.borderrun.app.domain.model

/**
 * String constants used as the `questionType` column value in
 * [com.borderrun.app.data.local.entity.QuizAnswerEntity].
 *
 * Stored in Room so the statistics screen can break down accuracy by type.
 */
object QuestionType {
    /** "What is the capital of [country]?" */
    const val CAPITAL = "capital"

    /** "Which country has this flag?" — shows a flag image. */
    const val FLAG = "flag"

    /** "Which country has a larger population?" — two-card comparison. */
    const val POPULATION_COMPARE = "population_compare"

    /** "[Capital] is the capital of which country?" */
    const val REVERSE_CAPITAL = "reverse_capital"

    /** "Which continent is [country] in?" */
    const val REGION = "region"

    /** "True or False: [country] is landlocked." */
    const val LANDLOCKED_TF = "landlocked_tf"
}

/**
 * Sealed hierarchy representing a single quiz question.
 *
 * Three subtypes cover the question formats used in Border Run:
 * - [MultipleChoice] — 4-option question, optionally with a flag image.
 * - [TrueFalse] — binary True / False question.
 * - [CompareTwo] — side-by-side country comparison.
 *
 * All subtypes share core properties so the ViewModel can handle them
 * uniformly for scoring and persistence.
 *
 * @property questionText The display text of the question.
 * @property correctAnswer The string that must be matched for a correct answer.
 * @property region The [Country.region] of the primary country — used for
 *   per-region accuracy stats.
 * @property difficulty One of `"easy"`, `"medium"`, `"hard"`.
 * @property explanationText Short fact shown after the answer is revealed.
 * @property questionType One of the [QuestionType] constants — stored in Room.
 * @property primaryCountryId cca3 of the country this question is about.
 */
sealed class QuizQuestion {
    abstract val questionText: String
    abstract val correctAnswer: String
    abstract val region: String
    abstract val difficulty: String
    abstract val explanationText: String
    abstract val questionType: String
    abstract val primaryCountryId: String

    /**
     * A question with four labelled answer options.
     *
     * @property options Shuffled list of four answer strings; always contains
     *   [correctAnswer].
     * @property flagUrl PNG flag URL shown above the options for
     *   [QuestionType.FLAG] questions; `null` for all other types.
     */
    data class MultipleChoice(
        override val questionText: String,
        override val correctAnswer: String,
        override val region: String,
        override val difficulty: String,
        override val explanationText: String,
        override val questionType: String,
        override val primaryCountryId: String,
        val options: List<String>,
        val flagUrl: String? = null,
    ) : QuizQuestion()

    /**
     * A binary True / False question.
     *
     * The [correctAnswer] is always `"True"` or `"False"`.
     */
    data class TrueFalse(
        override val questionText: String,
        override val correctAnswer: String,
        override val region: String,
        override val difficulty: String,
        override val explanationText: String,
        override val questionType: String,
        override val primaryCountryId: String,
    ) : QuizQuestion()

    /**
     * A side-by-side population comparison between two countries.
     *
     * The [correctAnswer] is the [Country.name] of the country with the larger
     * population. The UI renders [countryA] and [countryB] as tappable cards.
     *
     * @property countryA First country to compare.
     * @property countryB Second country to compare.
     */
    data class CompareTwo(
        override val questionText: String,
        override val correctAnswer: String,
        override val region: String,
        override val difficulty: String,
        override val explanationText: String,
        override val questionType: String,
        override val primaryCountryId: String,
        val countryA: Country,
        val countryB: Country,
    ) : QuizQuestion()
}
