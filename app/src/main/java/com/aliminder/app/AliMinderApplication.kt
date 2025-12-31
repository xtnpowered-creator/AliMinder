package com.aliminder.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * AliMinder Application class.
 * 
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation,
 * including a base class for the application that serves as the
 * application-level dependency container.
 */
@HiltAndroidApp
class AliMinderApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize application-level components
        // TODO: Initialize crash reporting (local-only if enabled)
        // TODO: Initialize audio engine service
        // TODO: Schedule initial vigilance checks
    }
}
