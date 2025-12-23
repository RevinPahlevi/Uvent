package com.example.uventapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.uventapp.MainActivity
import com.example.uventapp.R

object NotificationHelper {
    
    const val CHANNEL_ID = "event_feedback_channel"
    const val CHANNEL_NAME = "Event Feedback Reminders"
    const val CHANNEL_DESCRIPTION = "Notifikasi pengingat untuk memberikan feedback setelah event selesai"
    
    /**
     * Create notification channel (required for Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Show notification for event feedback
     * @param context Application context
     * @param eventId Event ID to navigate to
     * @param eventTitle Title of the event
     * @param notificationId Unique ID for this notification
     */
    fun showFeedbackNotification(
        context: Context,
        eventId: Int,
        eventTitle: String,
        notificationId: Int
    ) {
        // Create intent to open app with event ID
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "feedback")
            putExtra("event_id", eventId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId, // Use eventId as request code for uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.u) // Using app icon
            .setContentTitle("Event Selesai! 🎉")
            .setContentText("Bagaimana pengalamanmu di \"$eventTitle\"? Yuk beri feedback!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Event \"$eventTitle\" sudah selesai. Bagikan pengalamanmu dan bantu peserta lain dengan memberikan ulasan!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        // Show notification
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
            android.util.Log.e("NotificationHelper", "Notification permission not granted: ${e.message}")
        }
    }
    
    /**
     * Cancel a specific notification
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
