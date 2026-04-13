package com.borderrun.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.borderrun.app.domain.repository.CountryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that syncs country data from the RestCountries API into Room.
 *
 * Annotated with [@HiltWorker][HiltWorker] so that Hilt can inject dependencies
 * (e.g. [CountryRepository]) via the [HiltWorkerFactory] configured in
 * [com.borderrun.app.BorderRunApp].
 *
 * Scheduling:
 * - Runs once per day when the device has an unmetered network connection.
 * - Uses exponential back-off on failure (up to [MAX_RUN_ATTEMPTS] retries).
 * - Enqueue via [enqueue] to ensure only one periodic instance exists at a time.
 *
 * @property countryRepository Repository used to trigger the network sync and
 *   persist the result to Room.
 */
@HiltWorker
class ContentSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val countryRepository: CountryRepository,
) : CoroutineWorker(context, workerParams) {

    /**
     * Executes the sync operation.
     *
     * Calls [CountryRepository.syncCountries] to fetch fresh data from the
     * RestCountries API and upsert it into the local Room cache.
     *
     * @return [Result.success] if the sync completed without error;
     *   [Result.retry] if the attempt failed and retries remain;
     *   [Result.failure] if [MAX_RUN_ATTEMPTS] have been exhausted.
     */
    override suspend fun doWork(): Result {
        return try {
            countryRepository.syncCountries()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RUN_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    companion object {

        /**
         * Unique name used when enqueueing this worker so WorkManager can
         * replace or de-duplicate the periodic request.
         */
        const val WORK_NAME = "content_sync"

        /** Maximum number of run attempts before [Result.failure] is returned. */
        private const val MAX_RUN_ATTEMPTS = 3

        /** Sync interval — once every 24 hours. */
        private const val SYNC_INTERVAL_HOURS = 24L

        /**
         * Enqueues a unique periodic [ContentSyncWorker] that runs once a day
         * on an unmetered network.
         *
         * Uses [ExistingPeriodicWorkPolicy.KEEP] so rescheduling on app restart
         * does not reset the timer for an already-scheduled run.
         *
         * @param workManager The [WorkManager] instance to use for enqueueing.
         */
        fun enqueue(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            val request = PeriodicWorkRequestBuilder<ContentSyncWorker>(
                SYNC_INTERVAL_HOURS, TimeUnit.HOURS,
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
