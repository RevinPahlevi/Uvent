package com.example.uventapp

import android.app.Application
import android.util.Log
import com.example.uventapp.utils.NotificationHelper
import com.example.uventapp.worker.EventFeedbackWorker

/**
 * Application class that initializes WorkManager for background notifications
 * This ensures notifications work even when the app is not open
 */
class UventApplication : Application() {
    
    companion object {
        private const val TAG = "UventApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Application onCreate - initializing background services")
        
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        
        // Schedule the periodic background worker for event feedback notifications
        // This runs every 15 minutes even when app is closed
        EventFeedbackWorker.schedule(this)
        
        // Also run once immediately to check for any ended events
        EventFeedbackWorker.runOnce(this)
        
        Log.d(TAG, "Background notification worker scheduled")
    }
}
