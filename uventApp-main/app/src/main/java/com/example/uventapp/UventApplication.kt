package com.example.uventapp

import android.app.Application
import android.util.Log
import com.example.uventapp.utils.NotificationHelper

class UventApplication : Application() {
    
    companion object {
        private const val TAG = "UventApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Application onCreate - initializing services")
        
        NotificationHelper.createNotificationChannel(this)
        
        Log.d(TAG, "Notification channel created")
    }
}
