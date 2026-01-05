package com.aliminder.app.presentation.components

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*

/**
 * Rationale dialog explaining why location permission is needed.
 */
@Composable
fun LocationPermissionRationale(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Location Permission Required")
        },
        text = {
            Text(
                "AliMinder needs your location to calculate accurate travel times " +
                "and alert you when it's time to leave for your duties.\n\n" +
                "Your location data stays on your device and is only used for " +
                "Point of No Return (PoNR) calculations.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("Allow Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}

/**
 * Rationale dialog for Activity Recognition permission.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActivityRecognitionRationale(
    permissionState: PermissionState,
    onDismiss: () -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        // Not needed on Android 9 and below
        return
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Activity Detection Permission")
        },
        text = {
            Text(
                "AliMinder can detect when you're driving, walking, or stationary " +
                "to optimize battery usage.\n\n" +
                "This permission allows the app to use less battery by tracking " +
                "your location only when you're actually moving.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = {
                permissionState.launchPermissionRequest()
                onDismiss()
            }) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}

/**
 * Rationale dialog for Background Location permission.
 * Must be requested separately from foreground location on Android 10+.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BackgroundLocationRationale(
    permissionState: PermissionState,
    onDismiss: () -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        // Not needed on Android 9 and below
        return
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Background Location Permission")
        },
        text = {
            Text(
                "To provide accurate PoNR calculations even when the app is closed, " +
                "AliMinder needs permission to access your location in the background.\n\n" +
                "The app will only track your location when:\n" +
                "• You leave your home or work area\n" +
                "• You have an upcoming duty within 1 hour\n\n" +
                "Background tracking stops automatically when you're home with no upcoming duties.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = {
                permissionState.launchPermissionRequest()
                onDismiss()
            }) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}

/**
 * Dialog shown when permission is permanently denied.
 * Directs user to app settings.
 */
@Composable
fun PermissionDeniedDialog(
    permissionName: String,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Permission Required")
        },
        text = {
            Text(
                "$permissionName permission was denied. To use location features, " +
                "please enable this permission in your device settings.\n\n" +
                "Settings > Apps > AliMinder > Permissions",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
