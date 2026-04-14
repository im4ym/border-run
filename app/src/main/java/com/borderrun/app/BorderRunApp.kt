package com.borderrun.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.borderrun.app.worker.ContentSyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Custom [Application] class for Border Run.
 *
 * Annotated with [@HiltAndroidApp][HiltAndroidApp] to trigger Hilt's code
 * generation and initialise the application-level dependency graph.
 *
 * Implements [Configuration.Provider] so WorkManager is initialised with the
 * Hilt-aware [HiltWorkerFactory], enabling `@AssistedInject` in Workers.
 *
 * Implements [ImageLoaderFactory] to register [SvgDecoder.Factory] so that
 * country flag SVGs (e.g. Afghanistan, Nepal) render correctly via Coil's
 * singleton [ImageLoader].
 */
@HiltAndroidApp
class BorderRunApp : Application(), Configuration.Provider, ImageLoaderFactory {

    /** Injected [HiltWorkerFactory] for Hilt-aware WorkManager workers. */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * Provides the [Configuration] used to initialise WorkManager.
     *
     * Disabling auto-initialisation in the manifest is required when supplying
     * a custom factory (see [androidx.startup] or manifest merge rules).
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    /**
     * Returns a singleton [ImageLoader] with [SvgDecoder.Factory] registered.
     *
     * All [coil.compose.AsyncImage] calls across the app use this loader
     * automatically, enabling SVG flag images to display correctly.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()

    /**
     * Schedules the background country-data sync worker on every cold start.
     *
     * [ContentSyncWorker.enqueue] uses [ExistingPeriodicWorkPolicy.KEEP], so
     * calling this on each launch is safe — WorkManager will not reset the
     * timer if the worker is already queued.
     */
    override fun onCreate() {
        super.onCreate()
        ContentSyncWorker.enqueue(WorkManager.getInstance(this))
    }
}
