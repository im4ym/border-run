package com.borderrun.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.borderrun.app.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that posts a daily notification reminding the user to
 * complete their geography challenge.
 *
 * Scheduling:
 * - Runs once every 24 hours; no network constraint needed.
 * - Enqueue via [enqueue] / cancel via [cancel] — both are keyed on [WORK_NAME]
 *   so only one instance ever exists at a time.
 * - Only scheduled after the user has granted [android.Manifest.permission.POST_NOTIFICATIONS]
 *   (Android 13+) and turned on the daily-reminder toggle in Settings.
 *
 * Notification deep link:
 * - Tapping the notification opens `borderrun://quiz/daily/medium` which the
 *   [com.borderrun.app.MainActivity] intent filter resolves to the daily quiz.
 *
 * Privacy / ethics notes (ACS Code of Ethics, Assessment 2):
 * - **Data minimisation** — the notification contains no personal data; only a
 *   static reminder text is sent.
 * - **User autonomy** — the worker is only scheduled after explicit opt-in and
 *   is cancelled immediately when the user toggles the reminder off.
 */
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    /**
     * Posts the daily reminder notification.
     *
     * Creates the notification channel on first run (no-op on subsequent runs
     * per [NotificationManager] semantics), then posts the notification.
     *
     * @return [Result.success] always — notification posting is best-effort
     *   and a failure here should not block future runs.
     */
    override suspend fun doWork(): Result {
        ensureNotificationChannel()
        postNotification()
        return Result.success()
    }

    /**
     * Creates the [CHANNEL_ID] notification channel if it does not yet exist.
     *
     * Safe to call repeatedly — the system ignores duplicate channel creation.
     * Required for Android 8.0+ (API 26+).
     */
    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Builds and posts the daily reminder notification.
     *
     * The notification's content intent deep-links to the daily quiz via
     * `borderrun://quiz/daily/medium`. On Android 13+ (API 33+), the
     * [android.Manifest.permission.POST_NOTIFICATIONS] permission must be
     * granted for the notification to be posted — if it has been revoked since
     * this worker was scheduled, [NotificationManagerCompat] silently no-ops.
     */
    private fun postNotification() {
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(DEEP_LINK_URI),
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            PENDING_INTENT_REQUEST_CODE,
            deepLinkIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_BODY)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {

        /**
         * Unique work name used to de-duplicate the periodic request in
         * [WorkManager]'s internal queue.
         */
        const val WORK_NAME = "daily_reminder"

        /** Notification channel ID for daily reminder notifications. */
        private const val CHANNEL_ID = "daily_reminder"

        /** Human-readable channel name shown in Android notification settings. */
        private const val CHANNEL_NAME = "Daily Reminders"

        /** Channel description shown in Android notification settings. */
        private const val CHANNEL_DESCRIPTION = "Daily geography challenge reminders"

        /** Static title shown in the notification drawer. */
        private const val NOTIFICATION_TITLE = "Time to play Border Run! 🌍"

        /** Static body text shown in the notification drawer. */
        private const val NOTIFICATION_BODY = "Your daily geography challenge is waiting."

        /** URI handled by MainActivity's intent filter for the daily quiz deep link. */
        private const val DEEP_LINK_URI = "borderrun://quiz/daily/medium"

        /** Android notification ID — must be stable so updates replace the same row. */
        private const val NOTIFICATION_ID = 1001

        /** PendingIntent request code for the deep-link tap action. */
        private const val PENDING_INTENT_REQUEST_CODE = 0

        /** Reminder repeat interval — once every 24 hours. */
        private const val REMINDER_INTERVAL_HOURS = 24L

        /**
         * Enqueues a unique periodic [DailyReminderWorker] that runs once a day.
         *
         * Uses [ExistingPeriodicWorkPolicy.KEEP] so rescheduling on app restart
         * does not reset the timer for an already-pending run.
         *
         * @param workManager The [WorkManager] instance to use for enqueueing.
         */
        fun enqueue(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                REMINDER_INTERVAL_HOURS, TimeUnit.HOURS,
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        /**
         * Cancels the scheduled [DailyReminderWorker], if any.
         *
         * Called when the user turns off the daily-reminder toggle in Settings.
         *
         * @param workManager The [WorkManager] instance used to cancel the work.
         */
        fun cancel(workManager: WorkManager) {
            workManager.cancelUniqueWork(WORK_NAME)
        }
    }
}
