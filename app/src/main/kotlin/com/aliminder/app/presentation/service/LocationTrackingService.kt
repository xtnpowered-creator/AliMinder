package com.aliminder.app.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.aliminder.app.R
import com.aliminder.app.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground Service for background location tracking.
 * Ensures reliable location updates even when app is in background.
 * 
 * Shows persistent notification as required by Android for foreground services.
 */
@AndroidEntryPoint
class LocationTrackingService : Service() {
    
    @Inject
    lateinit var locationService: com.aliminder.app.domain.service.LocationService
    
    @Inject
    lateinit var dutyRepository: com.aliminder.app.domain.repository.DutyRepository
    
    companion object {
        private const val TAG = "LocationTrackingSvc"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_tracking"
        private const val CHANNEL_NAME = "Location Tracking"
        
        /**
         * Start location monitoring in foreground.
         */
        fun startMonitoring(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java)
            ContextCompat.startForegroundService(context, intent)
            Log.d(TAG, "Starting location tracking service")
        }
        
        /**
         * Stop location monitoring.
         */
        fun stopMonitoring(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java)
            context.stopService(intent)
            Log.d(TAG, "Stopping location tracking service")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        
        // Create and show foreground notification
        val notification = createNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        // Start location tracking and activity recognition
        locationService.startTracking()
        
        // Calculate nearest duty PoNR time for batching optimization
        CoroutineScope(Dispatchers.IO).launch {
            locationService.calculateAndUpdateNearestDutyPoNR(dutyRepository)
        }
        
        // Service will be restarted if killed by system
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        
        // Stop location tracking
        locationService.stopTracking()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        // Not a bound service
        return null
    }
    
    /**
     * Create notification channel for Android O+.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Low importance = no sound
            ).apply {
                description = "Shows when AliMinder is tracking your location for PoNR calculations"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create ongoing notification for foreground service.
     */
    private fun createNotification(): Notification {
        // Intent to open app when notification is tapped
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking Active")
            .setContentText("Calculating accurate PoNR times for your duties")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Android system location icon
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Cannot be dismissed
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
