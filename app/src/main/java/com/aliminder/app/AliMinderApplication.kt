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
        
        // Initialize Places API
        if (!com.google.android.libraries.places.api.Places.isInitialized()) {
            // Use the New Places API (resolves Error 9011)
            com.google.android.libraries.places.api.Places.initializeWithNewPlacesApiEnabled(applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
            android.util.Log.d("AddressEntry", "Places API (New) Initialized with Key: ${BuildConfig.GOOGLE_MAPS_API_KEY.take(5)}...")
        } else {
             android.util.Log.d("AddressEntry", "Places API already initialized")
        }
        
        Thread.setDefaultUncaughtExceptionHandler(com.aliminder.app.presentation.util.GlobalCrashHandler())
        scheduleProactiveLocationTracking()
        scheduleAutoHideWorker()
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

    private fun scheduleAutoHideWorker() {
        val workManager = androidx.work.WorkManager.getInstance(this)
        
        // Run every hour to keep the list fresh
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.aliminder.app.domain.worker.AutoHideDutiesWorker>(
            1, java.util.concurrent.TimeUnit.HOURS
        ).build()
        
        workManager.enqueueUniquePeriodicWork(
            "AutoHideDutiesWorker",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
