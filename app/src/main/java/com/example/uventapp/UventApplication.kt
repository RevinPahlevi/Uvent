package com.example.uventapp

import android.app.Application
import android.util.Log
import com.example.uventapp.utils.NotificationHelper

/**
 * Application class that initializes app-wide services
 * Note: Feedback notifications are now handled by backend via Firebase FCM
 * The EventFeedbackWorker has been replaced by backend scheduled task
 */
class UventApplication : Application() {
    
    companion object {
        private const val TAG = "UventApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Application onCreate - initializing services")
        
        // Create notification channel for FCM notifications
        NotificationHelper.createNotificationChannel(this)
        
        // Note: EventFeedbackWorker is no longer used
        // Feedback reminder notifications are now sent from backend via FCM
        // This ensures notification history is saved and appears in NotificationScreen
        
        Log.d(TAG, "Notification channel created")
    }
}
