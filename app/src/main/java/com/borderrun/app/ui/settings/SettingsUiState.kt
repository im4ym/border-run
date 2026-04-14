package com.borderrun.app.ui.settings

/**
 * UI state for the Settings screen.
 *
 * [SettingsViewModel] emits this via [SettingsViewModel.uiState]. The initial
 * value has [isLoading] `= true`; the first Room emission flips it to `false`.
 *
 * Only the four preferences shown in the Settings UI are exposed here. The
 * remaining [com.borderrun.app.data.local.entity.UserPreferencesEntity] fields
 * (timedMode, hintsEnabled, locationEnabled, notificationTime) are preserved
 * when writing back through the ViewModel's `_latestPrefs` snapshot.
 *
 * @property isLoading `true` while the preferences row has not yet been read.
 * @property difficulty Current quiz difficulty: `"easy"`, `"medium"`, or `"hard"`.
 * @property soundEnabled Whether sound effects play during quizzes.
 * @property notificationsEnabled Whether daily reminder notifications are active.
 * @property darkModeEnabled Whether dark mode is preferred (saved but not yet
 *   applied to the Material theme).
 */
data class SettingsUiState(
    val isLoading: Boolean = true,
    val difficulty: String = DEFAULT_DIFFICULTY,
    val soundEnabled: Boolean = DEFAULT_SOUND_ENABLED,
    val notificationsEnabled: Boolean = DEFAULT_NOTIFICATIONS_ENABLED,
    val darkModeEnabled: Boolean = DEFAULT_DARK_MODE_ENABLED,
)

// ── Default values ────────────────────────────────────────────────────────────

/** Default difficulty level used when no preferences row exists. */
const val DEFAULT_DIFFICULTY = "medium"

/** Default sound-effects setting used when no preferences row exists. */
const val DEFAULT_SOUND_ENABLED = true

/** Default notifications setting used when no preferences row exists. */
const val DEFAULT_NOTIFICATIONS_ENABLED = false

/** Default dark-mode setting used when no preferences row exists. */
const val DEFAULT_DARK_MODE_ENABLED = false
