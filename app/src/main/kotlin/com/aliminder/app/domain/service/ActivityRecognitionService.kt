package com.aliminder.app.domain.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for detecting user activity (STILL, WALKING, IN_VEHICLE, etc.)
 * to intelligently control location tracking.
 * 
 * Only triggers location updates when user is actually moving.
 */
@Singleton
class ActivityRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client: ActivityRecognitionClient = ActivityRecognition.getClient(context)
    
    companion object {
        private const val TAG = "ActivityRecognition"
        private const val ACTION_ACTIVITY_TRANSITION = "com.aliminder.app.ACTIVITY_TRANSITION"
        const val CONFIDENCE_THRESHOLD = 75 // Only act on high-confidence detections
    }
    
    /**
     * Start tracking activity transitions.
     * Emits DetectedActivity when user's activity changes with high confidence.
     */
    @SuppressLint("MissingPermission")
    fun startTracking(): Flow<DetectedActivity> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ACTION_ACTIVITY_TRANSITION) {
                    val activity = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra("activity", DetectedActivity::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<DetectedActivity>("activity")
                    }
                    
                    if (activity != null && activity.confidence >= CONFIDENCE_THRESHOLD) {
                        Log.d(TAG, "High-confidence activity detected: ${activityTypeToString(activity.type)} (${activity.confidence}%)")
                        trySend(activity)
                    }
                }
            }
        }
        
        // Register broadcast receiver
        context.registerReceiver(
            receiver,
            IntentFilter(ACTION_ACTIVITY_TRANSITION),
            Context.RECEIVER_NOT_EXPORTED
        )
        
        // Set up activity recognition
        val pendingIntent = createPendingIntent()
        
        try {
            // Request activity updates every 30 seconds
            client.requestActivityUpdates(
                30_000L, // 30 seconds
                pendingIntent
            ).await()
            
            Log.d(TAG, "Activity recognition started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start activity recognition", e)
            close(e)
        }
        
        awaitClose {
            Log.d(TAG, "Stopping activity recognition")
            context.unregisterReceiver(receiver)
            client.removeActivityUpdates(pendingIntent)
        }
    }
    
    /**
     * Create PendingIntent for activity recognition updates.
     */
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(ACTION_ACTIVITY_TRANSITION)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
    
    /**
     * Convert activity type constant to human-readable string.
     */
    private fun activityTypeToString(type: Int): String = when (type) {
        DetectedActivity.STILL -> "STILL"
        DetectedActivity.WALKING -> "WALKING"
        DetectedActivity.RUNNING -> "RUNNING"
        DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
        DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
        else -> "UNKNOWN"
    }
}
