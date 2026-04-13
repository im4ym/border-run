package com.borderrun.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.borderrun.app.data.local.entity.DailyMysteryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `daily_mystery` table.
 *
 * Each row represents a single day's Mystery Country puzzle. The puzzle is
 * generated alongside the daily challenge by [ContentSyncWorker].
 */
@Dao
interface DailyMysteryDao {

    /**
     * Inserts a new [DailyMysteryEntity] for a given day.
     *
     * @param mystery The mystery puzzle to insert.
     * @return The auto-generated primary key for the new row.
     */
    @Insert
    suspend fun insertMystery(mystery: DailyMysteryEntity): Long

    /**
     * Upserts a mystery — used to update progress ([cluesRevealed], [attempts], [solved]).
     *
     * @param mystery The updated mystery entity.
     */
    @Upsert
    suspend fun upsertMystery(mystery: DailyMysteryEntity)

    /**
     * Returns today's mystery puzzle as a reactive [Flow].
     *
     * [dayTimestamp] is the Unix timestamp (ms) at midnight UTC for today.
     *
     * @param dayTimestamp Midnight timestamp for the desired day.
     * @return Flow emitting the [DailyMysteryEntity] for that day, or `null`.
     */
    @Query("SELECT * FROM daily_mystery WHERE date = :dayTimestamp LIMIT 1")
    fun getMysteryForDay(dayTimestamp: Long): Flow<DailyMysteryEntity?>

    /**
     * Returns the mystery with the given [id].
     *
     * @param id Primary key of the mystery row.
     */
    @Query("SELECT * FROM daily_mystery WHERE id = :id")
    suspend fun getMysteryById(id: Int): DailyMysteryEntity?

    /**
     * Deletes all rows from the `daily_mystery` table.
     *
     * Called from "Clear All My Data" in Settings.
     */
    @Query("DELETE FROM daily_mystery")
    suspend fun deleteAll()
}
