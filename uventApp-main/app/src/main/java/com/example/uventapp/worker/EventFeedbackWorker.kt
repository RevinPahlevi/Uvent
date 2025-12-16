package com.example.uventapp.worker

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import com.example.uventapp.data.network.ApiClient
import com.example.uventapp.data.network.GetRegistrationsResponse
import com.example.uventapp.utils.NotificationHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * WorkManager Worker that periodically checks for ended events
 * and shows notifications to remind users to give feedback
 */
class EventFeedbackWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val WORK_NAME = "event_feedback_check_work"
        private const val TAG = "EventFeedbackWorker"
        private const val PREFS_NAME = "event_feedback_prefs"
        private const val KEY_NOTIFIED_EVENTS = "notified_events"
        
        /**
         * Schedule the periodic work to check for ended events
         * Runs every 15 minutes (minimum for periodic work)
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val periodicWork = PeriodicWorkRequestBuilder<EventFeedbackWorker>(
                15, TimeUnit.MINUTES // Check every 15 minutes (minimum interval)
            )
                .setConstraints(constraints)
                // No initial delay - start immediately
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                periodicWork
            )
            
            Log.d(TAG, "Scheduled periodic work for event feedback check")
        }
        
        /**
         * Run the worker immediately (for testing)
         */
        fun runOnce(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val oneTimeWork = OneTimeWorkRequestBuilder<EventFeedbackWorker>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueue(oneTimeWork)
            Log.d(TAG, "Enqueued one-time work for event feedback check")
        }
        
        /**
         * Cancel all scheduled work
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    override fun doWork(): Result {
        Log.d(TAG, "Starting event feedback check...")
        
        // Get user ID from shared preferences
        val userPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = userPrefs.getInt("user_id", -1)
        
        if (userId == -1) {
            Log.d(TAG, "No user logged in, skipping check")
            return Result.success()
        }
        
        Log.d(TAG, "Checking events for user: $userId")
        
        // Fetch registered events from API
        try {
            val response = ApiClient.instance.getRegisteredEvents(userId).execute()
            
            if (response.isSuccessful) {
                val registrations = response.body()?.data ?: emptyList()
                Log.d(TAG, "Found ${registrations.size} registered events")
                
                val now = Date()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val notifiedEvents = getNotifiedEvents()
                
                for (registration in registrations) {
                    val eventId = registration.eventId
                    val eventTitle = registration.title ?: "Event"
                    val eventDate = registration.date ?: continue
                    val eventEndTime = registration.timeEnd
                    
                    // Skip if already notified
                    if (notifiedEvents.contains(eventId.toString())) {
                        Log.d(TAG, "Already notified for event $eventId, skipping")
                        continue
                    }
                    
                    // Check if event has ended
                    try {
                        val eventDateParsed = dateFormat.parse(eventDate)
                        
                        if (eventDateParsed != null) {
                            // Combine date and end time
                            val calendar = Calendar.getInstance().apply {
                                time = eventDateParsed
                                if (eventEndTime != null) {
                                    try {
                                        val endTime = timeFormat.parse(eventEndTime)
                                        if (endTime != null) {
                                            val endCal = Calendar.getInstance().apply { time = endTime }
                                            set(Calendar.HOUR_OF_DAY, endCal.get(Calendar.HOUR_OF_DAY))
                                            set(Calendar.MINUTE, endCal.get(Calendar.MINUTE))
                                        }
                                    } catch (e: Exception) {
                                        // Default to end of day
                                        set(Calendar.HOUR_OF_DAY, 23)
                                        set(Calendar.MINUTE, 59)
                                    }
                                } else {
                                    // Default to end of day
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                }
                            }
                            
                            val eventEndDateTime = calendar.time
                            
                            if (now.after(eventEndDateTime)) {
                                Log.d(TAG, "Event $eventId ($eventTitle) has ended, showing notification")
                                
                                // Show notification
                                NotificationHelper.showFeedbackNotification(
                                    context = context,
                                    eventId = eventId,
                                    eventTitle = eventTitle,
                                    notificationId = eventId // Use event ID as notification ID
                                )
                                
                                // Mark as notified
                                markEventAsNotified(eventId)
                            } else {
                                Log.d(TAG, "Event $eventId has not ended yet: $eventEndDateTime")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing date for event $eventId: ${e.message}")
                    }
                }
            } else {
                Log.e(TAG, "Failed to fetch registrations: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking events: ${e.message}")
            return Result.retry()
        }
        
        Log.d(TAG, "Event feedback check completed")
        return Result.success()
    }
    
    private fun getNotifiedEvents(): Set<String> {
        return prefs.getStringSet(KEY_NOTIFIED_EVENTS, emptySet()) ?: emptySet()
    }
    
    private fun markEventAsNotified(eventId: Int) {
        val current = getNotifiedEvents().toMutableSet()
        current.add(eventId.toString())
        prefs.edit().putStringSet(KEY_NOTIFIED_EVENTS, current).apply()
    }
}
