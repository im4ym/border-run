package com.borderrun.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity recording the user's answer to a single quiz question.
 *
 * Links to [QuizSessionEntity] via [sessionId] and to [CountryEntity] via
 * [countryId]. Both foreign keys use `onDelete = CASCADE` so answers are
 * automatically removed when the parent session or country is deleted.
 *
 * @property id Auto-generated primary key.
 * @property sessionId Foreign key → [QuizSessionEntity.id].
 * @property questionType Type identifier, e.g. `"flag"`, `"capital"`, `"population_compare"`.
 * @property countryId Foreign key → [CountryEntity.id]; the primary country in the question.
 * @property userAnswer The option the user selected.
 * @property correctAnswer The correct answer string.
 * @property isCorrect Whether [userAnswer] equals [correctAnswer].
 * @property timeSpentMs Time the user spent on this question in milliseconds.
 * @property answeredAt Unix timestamp (ms) when the answer was submitted.
 */
@Entity(
    tableName = "quiz_answers",
    foreignKeys = [
        ForeignKey(
            entity = QuizSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["id"],
            childColumns = ["countryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("sessionId"),
        Index("countryId"),
    ],
)
data class QuizAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val questionType: String,
    val countryId: String,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timeSpentMs: Long,
    val answeredAt: Long,
)
