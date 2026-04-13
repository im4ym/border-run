package com.borderrun.app.domain.usecase

import com.borderrun.app.domain.model.DailyContent
import com.borderrun.app.domain.repository.DailyChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Use case that combines today's Daily Challenge and Mystery Country into a
 * single [DailyContent] stream.
 *
 * Either field of [DailyContent] may be `null` when
 * [com.borderrun.app.worker.ContentSyncWorker] has not yet generated content
 * for today (e.g. on first launch).
 *
 * @property dailyChallengeRepository Source of daily content data.
 */
class GetDailyContentUseCase @Inject constructor(
    private val dailyChallengeRepository: DailyChallengeRepository,
) {

    /**
     * Returns a [Flow] emitting fresh [DailyContent] whenever either the
     * challenge or the mystery row changes in Room.
     *
     * @param dayTimestamp Midnight UTC timestamp (ms) for the target day.
     *   Pass today's midnight timestamp to retrieve today's content.
     * @return Flow emitting [DailyContent].
     */
    operator fun invoke(dayTimestamp: Long): Flow<DailyContent> =
        combine(
            dailyChallengeRepository.getTodaysChallenge(dayTimestamp),
            dailyChallengeRepository.getTodaysMystery(dayTimestamp),
        ) { challenge, mystery ->
            DailyContent(challenge = challenge, mystery = mystery)
        }
}
