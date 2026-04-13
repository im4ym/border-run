package com.borderrun.app.domain.repository

import com.borderrun.app.domain.model.DailyChallengeInfo
import com.borderrun.app.domain.model.MysteryTeaser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for daily content (Daily Challenge and Mystery Country),
 * defined in the domain layer.
 *
 * The concrete implementation
 * ([com.borderrun.app.data.repository.DailyChallengeRepositoryImpl]) lives in
 * the data layer and is provided via Hilt.
 *
 * Offline-first: both functions read from Room. Content is generated and
 * inserted by [com.borderrun.app.worker.ContentSyncWorker] each day.
 */
interface DailyChallengeRepository {

    /**
     * Returns today's [DailyChallengeInfo] as a reactive [Flow].
     *
     * Emits `null` if [ContentSyncWorker][com.borderrun.app.worker.ContentSyncWorker]
     * has not yet generated a challenge for [dayTimestamp].
     *
     * @param dayTimestamp Unix timestamp (ms) at midnight UTC for today.
     * @return Flow emitting [DailyChallengeInfo], or `null`.
     */
    fun getTodaysChallenge(dayTimestamp: Long): Flow<DailyChallengeInfo?>

    /**
     * Returns today's [MysteryTeaser] as a reactive [Flow].
     *
     * Emits `null` if no mystery has been generated for [dayTimestamp] yet.
     *
     * @param dayTimestamp Unix timestamp (ms) at midnight UTC for today.
     * @return Flow emitting [MysteryTeaser], or `null`.
     */
    fun getTodaysMystery(dayTimestamp: Long): Flow<MysteryTeaser?>
}
