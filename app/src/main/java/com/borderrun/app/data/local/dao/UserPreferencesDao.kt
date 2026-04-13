package com.borderrun.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.borderrun.app.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `user_preferences` table.
 *
 * This is a single-row table (id is always `1`). Use [upsertPreferences] to
 * create or update, and [getPreferences] to observe changes reactively.
 */
@Dao
interface UserPreferencesDao {

    /**
     * Inserts or replaces the user preferences row.
     *
     * Because [UserPreferencesEntity.id] is always `1`, this effectively
     * acts as an update operation after the first call.
     *
     * @param preferences The preferences snapshot to persist.
     */
    @Upsert
    suspend fun upsertPreferences(preferences: UserPreferencesEntity)

    /**
     * Returns the user preferences row as a reactive [Flow].
     *
     * Emits `null` if preferences have never been saved (i.e. first launch).
     *
     * @return Flow emitting the single [UserPreferencesEntity] or `null`.
     */
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getPreferences(): Flow<UserPreferencesEntity?>

    /**
     * Deletes the preferences row.
     *
     * Called from "Clear All My Data" in Settings. After this call, the next
     * read will return `null` until defaults are written.
     */
    @Query("DELETE FROM user_preferences")
    suspend fun deleteAll()
}
