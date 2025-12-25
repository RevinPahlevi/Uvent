package com.example.uventapp.data.model

import com.google.gson.annotations.SerializedName

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
    val type: String,
    
    @SerializedName("related_id")
    val relatedId: Int?,
    
    @SerializedName("is_read")
    private val _isRead: Int,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("notification_data")
    val notificationData: Any? = null
) {
    val isRead: Boolean
        get() = _isRead != 0
}

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

data class MarkAsReadResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String
)
