package com.example.uventapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request untuk save FCM token
 */
data class SaveFCMTokenRequest(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("fcm_token")
    val fcmToken: String,
    
    @SerializedName("device_type")
    val deviceType: String = "android",
    
    @SerializedName("device_id")
    val deviceId: String? = null,
    
    @SerializedName("app_version")
    val appVersion: String? = null
)

/**
 * Response save FCM token
 */
data class SaveFCMTokenResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String
)
