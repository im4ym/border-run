package com.borderrun.app.data.repository

import com.borderrun.app.data.local.dao.DailyChallengeDao
import com.borderrun.app.data.local.dao.DailyMysteryDao
import com.borderrun.app.domain.model.DailyChallengeInfo
import com.borderrun.app.domain.model.MysteryTeaser
import com.borderrun.app.domain.repository.DailyChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Concrete implementation of [DailyChallengeRepository].
 *
 * Maps Room entities to domain models and delegates all reads to Room DAOs.
 *
 * @property dailyChallengeDao DAO for the `daily_challenges` table.
 * @property dailyMysteryDao DAO for the `daily_mystery` table.
 */
class DailyChallengeRepositoryImpl @Inject constructor(
    private val dailyChallengeDao: DailyChallengeDao,
    private val dailyMysteryDao: DailyMysteryDao,
) : DailyChallengeRepository {

    /**
     * Returns today's challenge as [DailyChallengeInfo], or `null` if none exists.
     *
     * Maps [com.borderrun.app.data.local.entity.DailyChallengeEntity] fields
     * to the domain model.
     *
     * @param dayTimestamp Midnight UTC timestamp for today.
     */
    override fun getTodaysChallenge(dayTimestamp: Long): Flow<DailyChallengeInfo?> =
        dailyChallengeDao.getTodaysChallenge(dayTimestamp).map { entity ->
            entity?.let {
                DailyChallengeInfo(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    region = it.region,
                    completed = it.completed,
                )
            }
        }

    /**
     * Returns today's mystery teaser as [MysteryTeaser], or `null` if none exists.
     *
     * Maps [com.borderrun.app.data.local.entity.DailyMysteryEntity] fields to
     * the domain model.
     *
     * @param dayTimestamp Midnight UTC timestamp for today.
     */
    override fun getTodaysMystery(dayTimestamp: Long): Flow<MysteryTeaser?> =
        dailyMysteryDao.getMysteryForDay(dayTimestamp).map { entity ->
            entity?.let {
                MysteryTeaser(
                    id = it.id,
                    solved = it.solved,
                    cluesRevealed = it.cluesRevealed,
                    attempts = it.attempts,
                )
            }
        }
}
