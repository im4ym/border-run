package com.borderrun.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for the Daily Mystery feature — a "guess the country" puzzle
 * where clues are progressively revealed until the user solves it or gives up.
 *
 * Each row represents one day's mystery. [countryId] links to the answer
 * country in [CountryEntity].
 *
 * @property id Auto-generated primary key.
 * @property date Unix timestamp (ms) at midnight (UTC) for the mystery's day.
 * @property countryId Foreign key → [CountryEntity.id]; the answer country.
 * @property cluesRevealed Number of clues the user has unlocked so far (0–5).
 * @property solved Whether the user correctly identified the mystery country.
 * @property attempts Total number of incorrect guesses made.
 * @property solvedAt Unix timestamp (ms) when the puzzle was solved; `null` if not yet solved.
 */
@Entity(
    tableName = "daily_mystery",
    foreignKeys = [
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["id"],
            childColumns = ["countryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("countryId")],
)
data class DailyMysteryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val countryId: String,
    val cluesRevealed: Int = 0,
    val solved: Boolean = false,
    val attempts: Int = 0,
    val solvedAt: Long? = null,
)
