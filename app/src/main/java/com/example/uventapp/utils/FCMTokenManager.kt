package com.example.uventapp.utils

import android.content.Context
import android.util.Log
import com.example.uventapp.data.network.ApiClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Helper untuk save FCM token ke backend
 */
object FCMTokenManager {
    
    private const val TAG = "FCMTokenManager"
    private const val PREFS_NAME = "fcm_prefs"
    private const val KEY_TOKEN_SENT = "token_sent_to_backend"
    
    /**
     * Get FCM token dan save ke backend
     * Call this after user login
     */
    fun saveFCMTokenToBackend(context: Context, userId: Int) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            
            val token = task.result
            Log.d(TAG, "FCM Token: $token")
            
            // Send token to backend
            sendTokenToBackend(context, userId, token)
        }
    }
    
    private fun sendTokenToBackend(context: Context, userId: Int, fcmToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = com.example.uventapp.data.model.SaveFCMTokenRequest(
                    userId = userId,
                    fcmToken = fcmToken,
                    deviceType = "android"
                )
                
                val call = ApiClient.instance.saveFCMToken(request)
                call.enqueue(object : Callback<com.example.uventapp.data.model.SaveFCMTokenResponse> {
                    override fun onResponse(
                        call: Call<com.example.uventapp.data.model.SaveFCMTokenResponse>,
                        response: Response<com.example.uventapp.data.model.SaveFCMTokenResponse>
                    ) {
                        if (response.isSuccessful) {
                            Log.d(TAG, "FCM token saved to backend successfully")
                            markTokenAsSent(context, fcmToken)
                        } else {
                            Log.e(TAG, "Failed to save FCM token: ${response.code()}")
                        }
                    }
                    
                    override fun onFailure(call: Call<com.example.uventapp.data.model.SaveFCMTokenResponse>, t: Throwable) {
                        Log.e(TAG, "Error saving FCM token", t)
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Exception saving FCM token", e)
            }
        }
    }
    
    private fun markTokenAsSent(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN_SENT, token)
            .apply()
    }
    
    fun isTokenSent(context: Context, token: String): Boolean {
        val sentToken = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN_SENT, null)
        return sentToken == token
    }
}
