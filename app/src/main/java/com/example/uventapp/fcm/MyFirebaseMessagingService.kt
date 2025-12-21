package com.example.uventapp.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.uventapp.MainActivity
import com.example.uventapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // TODO: Save token to backend after user logs in
        // This will be handled in MainActivity after login
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message received from: ${message.from}")
        Log.d(TAG, "Message data: ${message.data}")
        
        // Get notification type and related data
        val type = message.data["type"]
        val relatedId = message.data["related_id"]?.toIntOrNull() ?: 0
        val eventTitle = message.data["event_title"] ?: ""
        
        val title = message.notification?.title ?: "Uvent"
        val body = message.notification?.body ?: ""
        
        when (type) {
            "feedback_reminder" -> {
                Log.d(TAG, "Handling feedback_reminder for event: $relatedId")
                showFeedbackNotification(title, body, relatedId)
            }
            "event_approved" -> {
                // Notification when event is approved by admin
                Log.d(TAG, "Handling event_approved for event: $relatedId")
                showEventNotification(title, body, relatedId)
            }
            "event_created" -> {
                // Legacy support - redirect to event_approved handler
                Log.d(TAG, "Handling event_created (legacy) for event: $relatedId")
                showEventNotification(title, body, relatedId)
            }
            "new_feedback" -> {
                // Notification for event creator when someone gives feedback
                showEventNotification(title, body, relatedId)
            }
            else -> {
                // Generic notification
                showNotification(title, body)
            }
        }
    }
    
    /**
     * Show notification for feedback reminder - navigates to AddFeedbackScreen
     */
    private fun showFeedbackNotification(title: String, body: String, eventId: Int) {
        val channelId = "uvent_notifications"
        
        createNotificationChannel(channelId)
        
        // Intent to open app with deep link to feedback screen
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "feedback")
            putExtra("event_id", eventId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            eventId, // Use eventId as request code for uniqueness
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification_uvent)
            .setColor(0xFF4CAF50.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(eventId, notification)
        
        Log.d(TAG, "Feedback notification shown for event: $eventId")
    }
    
    /**
     * Show notification with deep link to event detail
     */
    private fun showEventNotification(title: String, body: String, eventId: Int) {
        val channelId = "uvent_notifications"
        
        createNotificationChannel(channelId)
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "event_detail")
            putExtra("event_id", eventId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            eventId + 10000, // Offset to avoid collision with feedback notifications
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification_uvent)
            .setColor(0xFF4CAF50.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(eventId + 10000, notification)
        
        Log.d(TAG, "Event notification shown for event: $eventId")
    }
    
    /**
     * Show generic notification (no deep link)
     */
    private fun showNotification(title: String, body: String) {
        val channelId = "uvent_notifications"
        
        createNotificationChannel(channelId)
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification_uvent)
            .setColor(0xFF4CAF50.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        
        Log.d(TAG, "Generic notification shown: $title")
    }
    
    /**
     * Create notification channel (Android 8.0+)
     */
    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Uvent Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for event registrations, feedback, and updates"
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        private const val TAG = "FCM_Service"
    }
}

