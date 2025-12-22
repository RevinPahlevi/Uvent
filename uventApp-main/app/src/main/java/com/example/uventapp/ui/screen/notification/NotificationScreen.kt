package com.example.uventapp.ui.screen.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.uventapp.data.model.Notification
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    userId: Int, // Pass dari ProfileViewModel atau login state
    viewModel: NotificationViewModel = viewModel()
) {
    // Fetch notifications saat screen pertama kali dimuat
    LaunchedEffect(userId) {
        viewModel.fetchNotifications(userId)
    }
    
    val notifications by viewModel.notifications
    val unreadCount by viewModel.unreadCount
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)
    
    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Notifikasi${if (unreadCount > 0) " ($unreadCount)" else ""}",
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = LightBackground
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refresh(userId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Loading state
                isLoading && notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                
                // Error state
                errorMessage != null && notifications.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "Terjadi kesalahan",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh(userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
                
                // Empty state
                notifications.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(LightBackground),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifikasi",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(80.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Belum Ada Notifikasi",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Notifikasi terbaru akan muncul di sini",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
                
                // List notifications
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LightBackground),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    // Mark as read when clicked
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification.id, userId)
                                    }
                                    
                                    // Navigate based on notification type
                                    val relatedId = notification.relatedId
                                    if (relatedId != null && relatedId > 0) {
                                        when (notification.type) {
                                            "feedback_reminder" -> {
                                                // Navigate to add feedback screen
                                                navController.navigate("add_feedback/$relatedId")
                                            }
                                            "documentation_reminder" -> {
                                                // Navigate to all documentation screen
                                                navController.navigate("all_documentation/$relatedId")
                                            }
                                            "new_feedback", "registration" -> {
                                                // Navigate to event detail
                                                navController.navigate("detail_event/$relatedId")
                                            }
                                            // Add more types as needed
                                        }
                                    }
                                }
                            )
                        }
                        
                        // Footer spacing
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFF0F9FF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen)
                        .align(Alignment.Top)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title with emoji
                Text(
                    text = notification.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                //Body message
                Text(
                    text = notification.body,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Timestamp
                Text(
                    text = formatTimestamp(notification.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Format timestamp ke format relatif (e.g., "2 jam yang lalu")
 */
private fun formatTimestamp(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(timestamp)
        val now = Date()
        val diff = now.time - (date?.time ?: 0)
        
        when {
            diff < 60000 -> "Baru saja"
            diff < 3600000 -> "${diff / 60000} menit yang lalu"
            diff < 86400000 -> "${diff / 3600000} jam yang lalu"
            diff < 604800000 -> "${diff / 86400000} hari yang lalu"
            else -> {
                val displayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                displayFormat.format(date)
            }
        }
    } catch (e: Exception) {
        timestamp
    }
}
