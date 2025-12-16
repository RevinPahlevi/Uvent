package com.example.uventapp.ui.screen.feedback

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.R
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.model.Feedback
import com.example.uventapp.data.model.dummyEvents
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.ui.screen.profile.ProfileViewModel
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFeedbackScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    profileViewModel: ProfileViewModel,
    eventId: Int?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Get current user profile
    val currentUserProfile by profileViewModel.profile
    val currentUserId = currentUserProfile?.id

    // State untuk loading
    var isLoading by remember { mutableStateOf(true) }

    // State untuk custom notification
    var showNotification by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf("") }
    var notificationIsSuccess by remember { mutableStateOf(true) }

    // Cari event dari ID
    val allEvents by viewModel.allEvents
    val event = remember(eventId, allEvents, viewModel.createdEvents.value, viewModel.followedEvents) {
        allEvents.find { it.id == eventId }
            ?: viewModel.createdEvents.value.find { it.id == eventId }
            ?: viewModel.followedEvents.find { it.id == eventId }
            ?: dummyEvents.find { it.id == eventId }
    }

    // Check if current user is the event creator
    val isEventCreator = remember(event, currentUserId) {
        event?.creatorId != null && event.creatorId == currentUserId
    }

    // Load feedbacks from API saat screen dibuka
    LaunchedEffect(eventId, currentUserId) {
        if (eventId != null && currentUserId != null) {
            viewModel.loadFeedbacksFromApi(eventId, currentUserId, context)
            delay(500)
            isLoading = false
        } else {
            isLoading = false
        }
    }

    // Ambil daftar feedback dari ViewModel
    val feedbackList by remember(viewModel.feedbacks.value, eventId) {
        derivedStateOf { viewModel.getFeedbacksForEvent(eventId ?: -1) }
    }

    // Hitung rata-rata rating
    val averageRating = remember(feedbackList) {
        if (feedbackList.isNotEmpty()) {
            feedbackList.map { it.rating }.average()
        } else {
            0.0
        }
    }

    // State untuk dialog hapus dan detail
    var showDeleteDialog by remember { mutableStateOf<Feedback?>(null) }
    var showDetailDialog by remember { mutableStateOf<Feedback?>(null) }

    // Observe notification message from ViewModel
    val vmNotificationMessage by viewModel.notificationMessage
    LaunchedEffect(vmNotificationMessage) {
        vmNotificationMessage?.let { message ->
            notificationMessage = message
            notificationIsSuccess = message.contains("berhasil", ignoreCase = true)
            showNotification = true
            viewModel.clearNotification()
            // Auto dismiss after 3 seconds
            delay(3000)
            showNotification = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CustomAppBar(title = "Semua Feedback", onBack = { navController.popBackStack() })
            },
            bottomBar = { BottomNavBar(navController = navController) },
            containerColor = LightBackground
        ) { paddingValues ->

            if (event == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Event tidak ditemukan.")
                }
                return@Scaffold
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // 1. Header Event dan Rating
                item {
                    EventRatingHeader(
                        event = event,
                        averageRating = averageRating,
                        totalReviews = feedbackList.size
                    )
                }

                // 2. Info untuk creator bahwa mereka tidak bisa memberi feedback
                if (isEventCreator) {
                    item {
                        CreatorInfoCard()
                    }
                }

                // 3. Tombol Tulis Feedback (HANYA muncul jika BUKAN creator DAN belum review)
                val hasUserReviewed = feedbackList.any { it.isAnda }
                if (!isEventCreator && !hasUserReviewed) {
                    item {
                        GradientButton(
                            text = "Tulis Feedback Anda",
                            onClick = { navController.navigate(Screen.AddFeedback.createRoute(event.id)) }
                        )
                    }
                }

                // 4. Empty state jika tidak ada feedback
                if (feedbackList.isEmpty()) {
                    item {
                        EmptyFeedbackState()
                    }
                }

                // 5. Daftar Feedback
                items(feedbackList, key = { it.id }) { feedback ->
                    FeedbackItemCard(
                        feedback = feedback,
                        isEventCreator = isEventCreator,
                        onLihatFeedbackClick = {
                            showDetailDialog = feedback
                        },
                        onEditClick = {
                            if (!isEventCreator) {
                                navController.navigate(Screen.AddFeedback.createRoute(event.id))
                            }
                        },
                        onDeleteClick = {
                            showDeleteDialog = feedback
                        }
                    )
                }
            }

            // 6. Dialog Hapus
            showDeleteDialog?.let { feedbackToDelete ->
                DeleteFeedbackDialog(
                    onDismiss = { showDeleteDialog = null },
                    onConfirm = {
                        viewModel.deleteFeedback(eventId!!, feedbackToDelete.id, currentUserId ?: 0, context)
                        showDeleteDialog = null
                    }
                )
            }

            // 7. Dialog Detail Feedback
            showDetailDialog?.let { feedbackDetail ->
                FeedbackDetailDialog(
                    feedback = feedbackDetail,
                    onDismiss = { showDetailDialog = null }
                )
            }
        }

        // Custom Notification Overlay
        if (showNotification) {
            CustomNotification(
                message = notificationMessage,
                isSuccess = notificationIsSuccess,
                onDismiss = { showNotification = false }
            )
        }
    }
}

// --- Custom Notification Component ---
@Composable
private fun CustomNotification(
    message: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .padding(top = 60.dp)
                .scale(scale)
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFE53935)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Tutup",
                        tint = White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// --- Gradient Button ---
@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(14.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF2E7D32), Color(0xFF43A047))
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.RateReview,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// --- Creator Info Card ---
@Composable
private fun CreatorInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFFE082), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.RateReview,
                    contentDescription = null,
                    tint = Color(0xFFFF8F00),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Sebagai pembuat event, Anda hanya dapat melihat feedback dari peserta.",
                fontSize = 14.sp,
                color = Color(0xFF5D4037),
                lineHeight = 20.sp
            )
        }
    }
}

// --- Empty State ---
@Composable
private fun EmptyFeedbackState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF5F5F5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SentimentDissatisfied,
                    contentDescription = null,
                    tint = Color(0xFFBDBDBD),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Belum Ada Feedback",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Jadilah yang pertama memberikan\nulasan untuk event ini!",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

// --- Event Rating Header ---
@Composable
private fun EventRatingHeader(
    event: Event,
    averageRating: Double,
    totalReviews: Int
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster with border
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.thumbnailUri ?: event.thumbnailResId ?: R.drawable.placeholder_poster)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.placeholder_poster),
                contentDescription = "Event Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.width(20.dp))

            // Rating section
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val ratingFormat = DecimalFormat("#.0")
                Text(
                    text = if (averageRating == 0.0) "N/A" else ratingFormat.format(averageRating),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
                // Animated stars
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { index ->
                        val starColor by animateColorAsState(
                            targetValue = if (index <= averageRating.toInt()) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                            animationSpec = tween(300),
                            label = "starColor"
                        )
                        Icon(
                            imageVector = if (index <= averageRating) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = starColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$totalReviews Ulasan",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// --- Feedback Item Card ---
@Composable
private fun FeedbackItemCard(
    feedback: Feedback,
    isEventCreator: Boolean,
    onLihatFeedbackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (feedback.isAnda) Color(0xFFF1F8E9) else White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (feedback.isAnda) Modifier.border(
                    2.dp,
                    PrimaryGreen.copy(alpha = 0.3f),
                    RoundedCornerShape(18.dp)
                ) else Modifier
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (feedback.isAnda)
                                    listOf(Color(0xFF43A047), Color(0xFF2E7D32))
                                else
                                    listOf(Color(0xFF90CAF9), Color(0xFF42A5F5))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = feedback.userName.take(1).uppercase(),
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                
                // Name and Date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${feedback.userName}${if (feedback.isAnda) " (Anda)" else ""}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (feedback.isAnda) PrimaryGreen else Color(0xFF1A1A1A)
                    )
                    Text(
                        text = feedback.postDate,
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
                
                // Rating Badge
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = feedback.rating.toString(),
                            color = Color(0xFFFF8F00),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Review text
            Text(
                text = feedback.review,
                fontSize = 15.sp,
                color = Color(0xFF424242),
                lineHeight = 24.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Feedback
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onLihatFeedbackClick() }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lihat Detail",
                        color = Color(0xFF1E88E5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Edit/Delete buttons (only for user's own feedback and not creator)
                if (feedback.isAnda && !isEventCreator) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Edit Button
                        Button(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E88E5),
                                contentColor = White
                            ),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        // Delete Button
                        Button(
                            onClick = onDeleteClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53935),
                                contentColor = White
                            ),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("Hapus", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// --- Feedback Detail Dialog ---
@Composable
private fun FeedbackDetailDialog(
    feedback: Feedback,
    onDismiss: () -> Unit
) {
    var showFullImage by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detail Feedback",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF757575)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // User info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF43A047), Color(0xFF2E7D32))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = feedback.userName.take(1).uppercase(),
                            color = White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = feedback.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = feedback.postDate,
                            fontSize = 13.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Rating
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { index ->
                        Icon(
                            imageVector = if (index <= feedback.rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = if (index <= feedback.rating) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${feedback.rating}/5",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF8F00)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Review text
                Text(
                    text = "Ulasan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = feedback.review,
                    fontSize = 16.sp,
                    color = Color(0xFF424242),
                    lineHeight = 26.sp
                )

                // Photo section (if photo exists)
                if (!feedback.photoUri.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Foto Suasana Event",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clickable { showFullImage = true }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(feedback.photoUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto Feedback",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                            )
                            
                            // Overlay hint to click for full view
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.ZoomIn,
                                        contentDescription = null,
                                        tint = White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Ketuk untuk perbesar",
                                        fontSize = 11.sp,
                                        color = White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Tutup", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
    
    // Full screen image viewer
    if (showFullImage && !feedback.photoUri.isNullOrEmpty()) {
        Dialog(
            onDismissRequest = { showFullImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { showFullImage = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(feedback.photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto Full Screen",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                
                // Close button at top right
                IconButton(
                    onClick = { showFullImage = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Tutup",
                        tint = White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// --- Delete Dialog ---
@Composable
private fun DeleteFeedbackDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.width(320.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFFFEBEE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🗑️", fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Hapus Ulasan?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ulasan yang dihapus tidak dapat dikembalikan.",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF757575)
                        )
                    ) {
                        Text("Batal", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            contentColor = White
                        )
                    ) {
                        Text("Hapus", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}