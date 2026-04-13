package com.borderrun.app.domain.usecase

import com.borderrun.app.domain.model.WeaknessData
import com.borderrun.app.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Minimum number of recorded answers before weakness data is considered reliable. */
const val MIN_ANSWERS_FOR_WEAKNESS = 20

/** Number of milliseconds in 30 days, used as the accuracy look-back window. */
private const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1_000

/**
 * Use case that emits [WeaknessData] for the Smart Weakness Trainer card on
 * the Home screen.
 *
 * Returns `null` when the user has fewer than [MIN_ANSWERS_FOR_WEAKNESS] total
 * answers — this prevents showing statistically unreliable suggestions to new
 * users.
 *
 * @property statsRepository Source of accuracy and answer-count data.
 */
class GetWeaknessDataUseCase @Inject constructor(
    private val statsRepository: StatsRepository,
) {

    /**
     * Returns a [Flow] that emits [WeaknessData] when there is enough data, or
     * `null` when the threshold has not been met.
     *
     * The look-back window is the most recent 30 days measured from collection
     * time.
     *
     * @return Flow emitting [WeaknessData] or `null`.
     */
    operator fun invoke(): Flow<WeaknessData?> {
        val since = System.currentTimeMillis() - THIRTY_DAYS_MS
        return combine(
            statsRepository.getWeakestRegion(since),
            statsRepository.getTotalAnswerCount(),
        ) { weakestRegion, totalCount ->
            val count = totalCount ?: 0
            if (weakestRegion != null && count >= MIN_ANSWERS_FOR_WEAKNESS) {
                WeaknessData(
                    regionName = weakestRegion.regionName,
                    accuracyFraction = weakestRegion.accuracyFraction,
                    totalAnswers = count,
                )
            } else {
                null
            }
        }
    }
}
