package com.borderrun.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity holding the user's app preferences.
 *
 * Designed as a single-row table: [id] is always `1`. Read and written via
 * upsert so there is never more than one row.
 *
 * @property id Constant primary key (`1`).
 * @property difficulty Default quiz difficulty: `"easy"`, `"medium"`, or `"hard"`.
 * @property timedMode Whether questions are shown with a countdown timer.
 * @property soundEnabled Whether sound effects play during quizzes.
 * @property hintsEnabled Whether hints are shown for difficult questions.
 * @property locationEnabled Whether coarse location is used for Local Discovery.
 * @property notificationsEnabled Whether daily reminder notifications are active.
 * @property notificationTime Preferred reminder time in `"HH:mm"` format (24-hour).
 */
@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val difficulty: String = "medium",
    val timedMode: Boolean = true,
    val soundEnabled: Boolean = true,
    val hintsEnabled: Boolean = false,
    val locationEnabled: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val notificationTime: String = "18:00",
    /** Whether the app should display in dark mode. Saved but not yet applied to the theme. */
    val darkModeEnabled: Boolean = false,
)
