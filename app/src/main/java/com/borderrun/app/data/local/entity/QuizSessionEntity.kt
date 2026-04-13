package com.borderrun.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a completed quiz session.
 *
 * @property id Auto-generated primary key.
 * @property gameMode One of `"classic"`, `"daily"`, `"streak"`, `"speed"`.
 * @property region Region filter used for the quiz, or `null` for streak/speed modes.
 * @property difficulty One of `"easy"`, `"medium"`, `"hard"`.
 * @property totalQuestions Number of questions presented.
 * @property correctAnswers Number of questions answered correctly.
 * @property score Total points earned in this session.
 * @property durationMs Total time taken for the quiz in milliseconds.
 * @property completedAt Unix timestamp (ms) when the session was finished.
 */
@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameMode: String,
    val region: String?,
    val difficulty: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Int,
    val durationMs: Long,
    val completedAt: Long,
)
