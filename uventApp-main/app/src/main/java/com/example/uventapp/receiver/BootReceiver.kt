package com.example.uventapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.uventapp.worker.EventFeedbackWorker

/**
 * BroadcastReceiver that listens for device boot completion
 * and reschedules the WorkManager job for background notifications
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d(TAG, "Device boot completed - rescheduling background worker")
            
            // Reschedule the periodic worker after device reboot
            EventFeedbackWorker.schedule(context)
            
            // Also run once immediately to check for any ended events
            EventFeedbackWorker.runOnce(context)
            
            Log.d(TAG, "Background worker rescheduled after boot")
        }
    }
}
