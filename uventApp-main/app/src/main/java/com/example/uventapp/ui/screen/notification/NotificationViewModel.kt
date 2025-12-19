package com.example.uventapp.ui.screen.notification

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uventapp.data.model.GetNotificationsResponse
import com.example.uventapp.data.model.Notification
import com.example.uventapp.data.network.ApiClient
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationViewModel : ViewModel() {
    
    private val _notifications = mutableStateOf<List<Notification>>(emptyList())
    val notifications: State<List<Notification>> = _notifications
    
    private val _unreadCount = mutableStateOf(0)
    val unreadCount: State<Int> = _unreadCount
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading
    
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage
    
    /**
     * Fetch notifications untuk user tertentu
     */
    fun fetchNotifications(userId: Int) {
        Log.d("NotifViewModel", "Fetching notifications for userId: $userId")
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            ApiClient.instance.getUserNotifications(userId).enqueue(object : Callback<GetNotificationsResponse> {
                override fun onResponse(
                    call: Call<GetNotificationsResponse>,
                    response: Response<GetNotificationsResponse>
                ) {
                    _isLoading.value = false
                    
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        _notifications.value = data?.notifications ?: emptyList()
                        _unreadCount.value = data?.unreadCount ?: 0
                        
                        Log.d("NotifViewModel", "Fetched ${_notifications.value.size} notifications")
                    } else {
                        _errorMessage.value = "Gagal memuat notifikasi"
                        Log.e("NotifViewModel", "Error: ${response.code()} - ${response.message()}")
                    }
                }
                
                override fun onFailure(call: Call<GetNotificationsResponse>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "Koneksi ke server gagal"
                    Log.e("NotifViewModel", "Failure: ${t.message}")
                }
            })
        }
    }
    
    /**
     * Mark notification as read
     */
    fun markAsRead(notificationId: Int, userId: Int) {
        viewModelScope.launch {
            ApiClient.instance.markNotificationAsRead(notificationId).enqueue(object : Callback<com.example.uventapp.data.model.MarkAsReadResponse> {
                override fun onResponse(
                    call: Call<com.example.uventapp.data.model.MarkAsReadResponse>,
                    response: Response<com.example.uventapp.data.model.MarkAsReadResponse>
                ) {
                    if (response.isSuccessful) {
                        // Update local state
                        _notifications.value = _notifications.value.map { notif ->
                            if (notif.id == notificationId) {
                                notif.copy(_isRead = 1)  // MySQL: 1 = true
                            } else {
                                notif
                            }
                        }
                        
                        // Update unread count
                        _unreadCount.value = _notifications.value.count { !it.isRead }
                        
                        Log.d("NotifViewModel", "Marked notification $notificationId as read")
                    }
                }
                
                override fun onFailure(call: Call<com.example.uventapp.data.model.MarkAsReadResponse>, t: Throwable) {
                    Log.e("NotifViewModel", "Failed to mark as read: ${t.message}")
                }
            })
        }
    }
    
    /**
     * Mark all notifications as read
     */
    fun markAllAsRead(userId: Int) {
        viewModelScope.launch {
            ApiClient.instance.markAllNotificationsAsRead(userId).enqueue(object : Callback<com.example.uventapp.data.model.MarkAsReadResponse> {
                override fun onResponse(
                    call: Call<com.example.uventapp.data.model.MarkAsReadResponse>,
                    response: Response<com.example.uventapp.data.model.MarkAsReadResponse>
                ) {
                    if (response.isSuccessful) {
                        // Update all to read
                        _notifications.value = _notifications.value.map { it.copy(_isRead = 1) }  // MySQL: 1 = true
                        _unreadCount.value = 0
                        
                        Log.d("NotifViewModel", "Marked all notifications as read")
                    }
                }
                
                override fun onFailure(call: Call<com.example.uventapp.data.model.MarkAsReadResponse>, t: Throwable) {
                    Log.e("NotifViewModel", "Failed to mark all as read: ${t.message}")
                }
            })
        }
    }
    
    /**
     * Refresh notifications (untuk pull-to-refresh)
     */
    fun refresh(userId: Int) {
        fetchNotifications(userId)
    }
}
