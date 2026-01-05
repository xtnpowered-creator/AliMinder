package com.aliminder.app.presentation.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.*

/**
 * Manages the multi-step permission request flow for location features.
 * 
 * Flow:
 * 1. Request ACCESS_FINE_LOCATION
 * 2. Request ACTIVITY_RECOGNITION (Android 10+)
 * 3. Request ACCESS_BACKGROUND_LOCATION separately (Android 10+)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionsManager(
    onAllPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Step 1: Foreground location permission
    val locationPermission = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    // Step 2: Activity recognition (Android 10+)
    val activityPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACTIVITY_RECOGNITION)
    } else {
        null
    }
    
    // Step 3: Background location (Android 10+)
    val backgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        null
    }
    
    var currentStep by remember { mutableStateOf(PermissionStep.LOCATION) }
    var showRationale by remember { mutableStateOf(false) }
    var showDeniedDialog by remember { mutableStateOf(false) }
    
    // Check if all granted
    LaunchedEffect(
        locationPermission.status,
        activityPermission?.status,
        backgroundLocationPermission?.status
    ) {
        val allGranted = locationPermission.status.isGranted &&
            (activityPermission == null || activityPermission.status.isGranted) &&
            (backgroundLocationPermission == null || backgroundLocationPermission.status.isGranted)
        
        if (allGranted) {
            onAllPermissionsGranted()
        }
    }
    
    // Permission flow state machine
    when (currentStep) {
        PermissionStep.LOCATION -> {
            when {
                locationPermission.status.isGranted -> {
                    // Move to next step
                    currentStep = if (activityPermission != null) {
                        PermissionStep.ACTIVITY_RECOGNITION
                    } else {
                        PermissionStep.COMPLETED
                    }
                }
                locationPermission.status.shouldShowRationale || showRationale -> {
                    LocationPermissionRationale(
                        onRequestPermission = {
                            locationPermission.launchPermissionRequest()
                            showRationale = false
                        },
                        onDismiss = {
                            showRationale = false
                            onPermissionsDenied()
                        }
                    )
                }
                !locationPermission.status.isGranted && !showRationale -> {
                    // Show rationale first time
                    showRationale = true
                }
            }
            
            // Handle permanent denial
            if (locationPermission.status is PermissionStatus.Denied &&
                !(locationPermission.status as PermissionStatus.Denied).shouldShowRationale) {
                showDeniedDialog = true
            }
        }
        
        PermissionStep.ACTIVITY_RECOGNITION -> {
            activityPermission?.let { permission ->
                when {
                    permission.status.isGranted -> {
                        currentStep = if (backgroundLocationPermission != null) {
                            PermissionStep.BACKGROUND_LOCATION
                        } else {
                            PermissionStep.COMPLETED
                        }
                    }
                    permission.status.shouldShowRationale || showRationale -> {
                        ActivityRecognitionRationale(
                            permissionState = permission,
                            onDismiss = {
                                showRationale = false
                                // Activity recognition is optional, move to next step
                                currentStep = if (backgroundLocationPermission != null) {
                                    PermissionStep.BACKGROUND_LOCATION
                                } else {
                                    PermissionStep.COMPLETED
                                }
                            }
                        )
                    }
                    !permission.status.isGranted && !showRationale -> {
                        showRationale = true
                    }
                }
            }
        }
        
        PermissionStep.BACKGROUND_LOCATION -> {
            backgroundLocationPermission?.let { permission ->
                when {
                    permission.status.isGranted -> {
                        currentStep = PermissionStep.COMPLETED
                    }
                    permission.status.shouldShowRationale || showRationale -> {
                        BackgroundLocationRationale(
                            permissionState = permission,
                            onDismiss = {
                                showRationale = false
                                // Background location is optional, complete
                                currentStep = PermissionStep.COMPLETED
                            }
                        )
                    }
                    !permission.status.isGranted && !showRationale -> {
                        showRationale = true
                    }
                }
            }
        }
        
        PermissionStep.COMPLETED -> {
            // All done
        }
    }
    
    // Show permanently denied dialog
    if (showDeniedDialog) {
        PermissionDeniedDialog(
            permissionName = "Location",
            onOpenSettings = {
                openAppSettings(context)
                showDeniedDialog = false
            },
            onDismiss = {
                showDeniedDialog = false
                onPermissionsDenied()
            }
        )
    }
}

/**
 * Permission request steps.
 */
private enum class PermissionStep {
    LOCATION,
    ACTIVITY_RECOGNITION,
    BACKGROUND_LOCATION,
    COMPLETED
}

/**
 * Open app settings page.
 */
private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}
