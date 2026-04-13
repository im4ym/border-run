package com.borderrun.app.data.local.model

/**
 * Projection returned by accuracy-by-region queries.
 *
 * @property region The continental region name.
 * @property total Total number of answers recorded for this region.
 * @property correct Number of those answers that were correct.
 */
data class RegionAccuracy(
    val region: String,
    val total: Int,
    val correct: Int,
) {
    /** Accuracy as a [Float] in the range `0.0..1.0`. */
    val accuracyFraction: Float
        get() = if (total == 0) 0f else correct.toFloat() / total.toFloat()
}

/**
 * Projection returned by the weekly activity query.
 *
 * @property day Date string in `"YYYY-MM-DD"` format.
 * @property count Number of answers recorded on that day.
 */
data class DayActivity(
    val day: String,
    val count: Int,
)

/**
 * Projection returned by the question-type accuracy query.
 *
 * @property questionType Type key, e.g. `"flag"`, `"capital"`.
 * @property accuracy Accuracy fraction in the range `0.0..1.0`.
 */
data class QuestionTypeAccuracy(
    val questionType: String,
    val accuracy: Float,
)
