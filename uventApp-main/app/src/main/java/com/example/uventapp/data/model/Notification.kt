package com.example.uventapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class untuk Notification
 * Sesuai dengan struktur database backend
 * 
 * Note: is_read dari MySQL BOOLEAN (TINYINT) returns 0/1, bukan true/false
 */
data class Notification(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("body")
    val body: String,
    
    @SerializedName("type")
    val type: String, // registration, feedback, general, etc.
    
    @SerializedName("related_id")
    val relatedId: Int?,
    
    @SerializedName("is_read")
    private val _isRead: Int, // MySQL returns 0 or 1
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("notification_data")
    val notificationData: Any? = null  // Can be String or JSON Object from backend
) {
    // Convert MySQL int (0/1) to Boolean
    val isRead: Boolean
        get() = _isRead != 0
}

/**
 * Response untuk GET /api/notifications/user/:userId
 */
data class GetNotificationsResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("data")
    val data: NotificationData
)

data class NotificationData(
    @SerializedName("notifications")
    val notifications: List<Notification>,
    
    @SerializedName("unread_count")
    val unreadCount: Int,
    
    @SerializedName("total")
    val total: Int
)

/**
 * Response untuk mark as read
 */
data class MarkAsReadResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String
)
