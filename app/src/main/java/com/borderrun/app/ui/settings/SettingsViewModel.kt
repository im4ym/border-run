package com.borderrun.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.borderrun.app.data.local.dao.QuizAnswerDao
import com.borderrun.app.data.local.dao.QuizSessionDao
import com.borderrun.app.data.local.dao.UserPreferencesDao
import com.borderrun.app.data.local.entity.UserPreferencesEntity
import com.borderrun.app.worker.DailyReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 *
 * Reads and writes [UserPreferencesEntity] via [UserPreferencesDao]. Each
 * toggle or selection triggers an immediate upsert so the preference is
 * persisted without an explicit "Save" action.
 *
 * A full [UserPreferencesEntity] snapshot ([_latestPrefs]) is kept in sync with
 * Room via [onEach] so that updating one field does not accidentally reset
 * unrelated fields (e.g. [UserPreferencesEntity.timedMode]) that are not
 * surfaced in this screen.
 *
 * @property userPreferencesDao DAO for the single-row `user_preferences` table.
 * @property quizSessionDao DAO for deleting sessions on "Clear Progress".
 * @property quizAnswerDao DAO for deleting answers on "Clear Progress".
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao,
    private val quizSessionDao: QuizSessionDao,
    private val quizAnswerDao: QuizAnswerDao,
    @ApplicationContext context: Context,
) : ViewModel() {

    /** WorkManager instance used to schedule and cancel the daily reminder. */
    private val workManager: WorkManager = WorkManager.getInstance(context)

    /**
     * Full entity snapshot kept in sync with Room.
     *
     * When updating a single field, [copy] is called on this value so every
     * other field is preserved — including fields not shown in the UI.
     */
    private val _latestPrefs = MutableStateFlow(UserPreferencesEntity())

    /**
     * Unified Settings screen state.
     *
     * Derived from [UserPreferencesDao.getPreferences]; uses [onEach] to
     * keep [_latestPrefs] up to date with the Room source of truth. Emits
     * [SettingsUiState.isLoading] `= true` as the initial value until the
     * first Room row arrives.
     */
    val uiState: StateFlow<SettingsUiState> = userPreferencesDao.getPreferences()
        .onEach { entity -> if (entity != null) _latestPrefs.value = entity }
        .map { entity ->
            SettingsUiState(
                isLoading = false,
                difficulty = entity?.difficulty ?: DEFAULT_DIFFICULTY,
                soundEnabled = entity?.soundEnabled ?: DEFAULT_SOUND_ENABLED,
                notificationsEnabled = entity?.notificationsEnabled ?: DEFAULT_NOTIFICATIONS_ENABLED,
                darkModeEnabled = entity?.darkModeEnabled ?: DEFAULT_DARK_MODE_ENABLED,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = SettingsUiState(isLoading = true),
        )

    // ── Setting updaters ──────────────────────────────────────────────────────

    /**
     * Persists a new difficulty level immediately.
     *
     * @param difficulty One of `"easy"`, `"medium"`, or `"hard"`.
     */
    fun setDifficulty(difficulty: String) {
        save(_latestPrefs.value.copy(difficulty = difficulty))
    }

    /**
     * Persists the sound-effects preference immediately.
     *
     * @param enabled `true` to enable sound effects during quizzes.
     */
    fun setSoundEnabled(enabled: Boolean) {
        save(_latestPrefs.value.copy(soundEnabled = enabled))
    }

    /**
     * Persists the daily-reminder notification preference immediately.
     *
     * @param enabled `true` to receive daily reminder notifications.
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        save(_latestPrefs.value.copy(notificationsEnabled = enabled))
    }

    /**
     * Persists the dark-mode preference immediately.
     *
     * The preference is saved to Room but the theme is not yet switched at
     * runtime (non-functional in this release).
     *
     * @param enabled `true` to prefer dark mode.
     */
    fun setDarkModeEnabled(enabled: Boolean) {
        save(_latestPrefs.value.copy(darkModeEnabled = enabled))
    }

    // ── Worker scheduling ─────────────────────────────────────────────────────

    /**
     * Enqueues the [DailyReminderWorker] for periodic 24-hour execution.
     *
     * Called by the UI layer after [android.Manifest.permission.POST_NOTIFICATIONS]
     * has been granted (Android 13+) and the user has turned the daily-reminder
     * toggle on. Safe to call multiple times — WorkManager de-duplicates via
     * [DailyReminderWorker.WORK_NAME].
     */
    fun scheduleReminderWorker() {
        DailyReminderWorker.enqueue(workManager)
    }

    /**
     * Cancels the [DailyReminderWorker] if it is scheduled.
     *
     * Called immediately when the user turns the daily-reminder toggle off,
     * upholding the user-autonomy principle from Assessment 2 (ACS Clause 1.2.1).
     */
    fun cancelReminderWorker() {
        DailyReminderWorker.cancel(workManager)
    }

    // ── Data management ───────────────────────────────────────────────────────

    /**
     * Deletes all quiz sessions and answers from Room.
     *
     * Called after the user confirms the "Clear Progress" dialog. The
     * [StatsViewModel][com.borderrun.app.ui.stats.StatsViewModel] will
     * automatically reflect the cleared state via its Room flows.
     */
    fun clearAllProgress() {
        viewModelScope.launch {
            quizSessionDao.deleteAll()
            quizAnswerDao.deleteAll()
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Updates [_latestPrefs] and upserts [prefs] to Room on [viewModelScope].
     *
     * @param prefs The full entity to persist.
     */
    private fun save(prefs: UserPreferencesEntity) {
        _latestPrefs.value = prefs
        viewModelScope.launch { userPreferencesDao.upsertPreferences(prefs) }
    }

    companion object {
        /** Delay in ms before upstream flows are cancelled after the last subscriber. */
        private const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
