package com.borderrun.app.domain.model

/**
 * Domain model returned by [com.borderrun.app.domain.repository.StatsRepository]
 * for the weakest region, before the answer-count threshold is applied.
 *
 * This type lives in the domain layer so the repository interface remains free
 * of data-layer imports.
 *
 * @property regionName Continental region name, e.g. `"Asia"`.
 * @property accuracyFraction Correct-answer rate in `0.0..1.0`.
 */
data class WeaknessInfo(
    val regionName: String,
    val accuracyFraction: Float,
)

/**
 * Describes the user's weakest region, enriched with the total answer count.
 *
 * Created by [com.borderrun.app.domain.usecase.GetWeaknessDataUseCase] only
 * when [totalAnswers] exceeds the minimum threshold, so the caller is
 * guaranteed meaningful data.
 *
 * @property regionName Continental region with the lowest accuracy, e.g. `"Asia"`.
 * @property accuracyFraction Fraction of correct answers in that region (`0.0..1.0`).
 * @property totalAnswers Total answers recorded (used to verify the threshold was met).
 */
data class WeaknessData(
    val regionName: String,
    val accuracyFraction: Float,
    val totalAnswers: Int,
)
