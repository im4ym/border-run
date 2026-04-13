package com.borderrun.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
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
 */
@HiltAndroidApp
class BorderRunApp : Application(), Configuration.Provider {

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
}
