package com.borderrun.app.domain.model

/** Number of questions in every Daily Challenge. */
const val DAILY_CHALLENGE_QUESTION_COUNT = 10

/** Estimated minutes to complete one Daily Challenge. */
const val DAILY_CHALLENGE_TIME_ESTIMATE_MINUTES = 5

/**
 * Domain model for the Daily Challenge card shown on the Home screen.
 *
 * Mapped from [com.borderrun.app.data.local.entity.DailyChallengeEntity] by
 * [com.borderrun.app.data.repository.DailyChallengeRepositoryImpl].
 *
 * @property id Primary key of the challenge row.
 * @property title Short display title, e.g. `"Island Nations"`.
 * @property description One-line summary shown beneath the title.
 * @property region Optional region filter; `null` means mixed-region.
 * @property completed Whether the user has already finished today's challenge.
 */
data class DailyChallengeInfo(
    val id: Int,
    val title: String,
    val description: String,
    val region: String?,
    val completed: Boolean,
)

/**
 * Lightweight domain model for the Daily Mystery Country teaser on the Home screen.
 *
 * The full mystery experience lives on a dedicated screen; this is only the
 * summary needed to render the home card.
 *
 * @property id Primary key of the mystery row.
 * @property solved Whether the user correctly identified today's mystery country.
 * @property cluesRevealed Number of clues unlocked so far (0–5).
 * @property attempts Total incorrect guesses made.
 */
data class MysteryTeaser(
    val id: Int,
    val solved: Boolean,
    val cluesRevealed: Int,
    val attempts: Int,
)

/**
 * Container combining today's [DailyChallengeInfo] and [MysteryTeaser].
 *
 * Either field may be `null` if WorkManager has not yet generated today's
 * content (e.g. on first launch before the initial sync).
 *
 * @property challenge Today's Daily Challenge, or `null` if not yet generated.
 * @property mystery Today's Mystery Country teaser, or `null` if not yet generated.
 */
data class DailyContent(
    val challenge: DailyChallengeInfo?,
    val mystery: MysteryTeaser?,
)
