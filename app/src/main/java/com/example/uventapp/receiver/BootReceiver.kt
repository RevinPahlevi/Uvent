package com.example.uventapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver that listens for device boot completion.
 * 
 * Note: EventFeedbackWorker has been removed.
 * Feedback notifications are now handled by backend via Firebase FCM.
 * This receiver is kept for potential future use cases.
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d(TAG, "Device boot completed")
            
            // Note: EventFeedbackWorker is no longer used
            // Feedback reminder notifications are now sent from backend via FCM
            // No action needed on device boot
        }
    }
}
