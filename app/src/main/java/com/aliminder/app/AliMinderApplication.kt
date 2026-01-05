package com.aliminder.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AliMinderApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    override fun onCreate() {
        super.onCreate()
        scheduleProactiveLocationTracking()
    }
    
    /**
     * Schedule periodic worker to check for upcoming duties and start location tracking proactively.
     */
    private fun scheduleProactiveLocationTracking() {
        val workManager = androidx.work.WorkManager.getInstance(this)
        
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.aliminder.app.domain.worker.ProactiveLocationTrackingWorker>(
            30, java.util.concurrent.TimeUnit.MINUTES
        ).build()
        
        workManager.enqueueUniquePeriodicWork(
            com.aliminder.app.domain.worker.ProactiveLocationTrackingWorker.WORK_NAME,
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
