package com.aliminder.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aliminder.app.domain.service.LocationService // Added import
import com.aliminder.app.domain.worker.AutoHideDutiesWorker
import com.aliminder.app.presentation.components.LocationPermissionsManager
import com.aliminder.app.presentation.navigation.AppNavigation
import com.aliminder.app.presentation.theme.AliMinderTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for AliMinder.
 * 
 * Single-activity architecture using Jetpack Compose and Navigation.
 * Annotated with @AndroidEntryPoint to enable Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var locationService: LocationService // Injected service
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lifecycle observer to trigger auto-hide check when app is started
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                // App entered foreground
                locationService.setForegroundState(true)
                
                val workManager = WorkManager.getInstance(applicationContext)
                val oneTimeWorkRequest = OneTimeWorkRequestBuilder<AutoHideDutiesWorker>().build()
                workManager.enqueue(oneTimeWorkRequest)
            } else if (event == Lifecycle.Event.ON_STOP) {
                // App entered background
                locationService.setForegroundState(false)
            }
        }

        lifecycle.addObserver(lifecycleObserver)
        
        setContent {
            var showPermissionRequest by remember { 
                mutableStateOf(!hasLocationPermissions())
            }
            
            AliMinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showPermissionRequest) {
                        LocationPermissionsManager(
                            onAllPermissionsGranted = {
                                showPermissionRequest = false
                            },
                            onPermissionsDenied = {
                                // User can still use app without location features
                                showPermissionRequest = false
                            }
                        )
                    }
                    
                    AppNavigation()
                }
            }
        }
    }
    
    /**
     * Check if basic location permissions are granted.
     */
    private fun hasLocationPermissions(): Boolean {
        return checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
