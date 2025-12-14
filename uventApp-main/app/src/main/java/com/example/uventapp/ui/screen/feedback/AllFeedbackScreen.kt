package com.example.uventapp.ui.screen.feedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward // Import ikon panah
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.R
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.model.Feedback
// Removed dummy events import
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFeedbackScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    eventId: Int?
) {
    val context = LocalContext.current
    
    // Cari event dari ID
    val event = remember(eventId, viewModel.allEvents.value, viewModel.createdEvents.value, viewModel.followedEvents) {
        (viewModel.allEvents.value + viewModel.createdEvents.value + viewModel.followedEvents).find { it.id == eventId }
    }

    // Ambil daftar feedback dari ViewModel
    val feedbackList by remember(viewModel.feedbacks.value) {
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

    // State untuk dialog hapus
    var showDeleteDialog by remember { mutableStateOf<Feedback?>(null) }

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

            // 2. Tombol Tulis Feedback (hanya muncul jika user belum review)
            val hasUserReviewed = feedbackList.any { it.isAnda }
            if (!hasUserReviewed) {
                item {
                    Button(
                        onClick = { navController.navigate(Screen.AddFeedback.createRoute(event.id)) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tulis Feedback Anda")
                    }
                }
            }

            // 3. Daftar Feedback
            items(feedbackList, key = { it.id }) { feedback ->
                FeedbackItemCard(
                    feedback = feedback,
                    onLihatFeedbackClick = {
                        // TODO: Navigasi ke DetailFeedbackScreen (jika ada)
                        // Untuk saat ini, kita bisa navigasi ke AddFeedback
                        // jika itu milik "Anda", atau tidak melakukan apa-apa
                        if (feedback.isAnda) {
                            navController.navigate(Screen.AddFeedback.createRoute(event.id))
                        }
                    },
                    onEditClick = {
                        navController.navigate(Screen.AddFeedback.createRoute(event.id))
                    },
                    onDeleteClick = {
                        showDeleteDialog = feedback
                    }
                )
            }
        }

        // 4. Dialog Hapus
        showDeleteDialog?.let { feedbackToDelete ->
            DeleteFeedbackDialog(
                onDismiss = { showDeleteDialog = null },
                onConfirm = {
                    viewModel.deleteFeedback(eventId!!, feedbackToDelete.id, 0, context)
                    showDeleteDialog = null
                }
            )
        }
    }
}

// --- Composable Kustom untuk Halaman Ini ---

@Composable
private fun EventRatingHeader(
    event: Event,
    averageRating: Double,
    totalReviews: Int
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.thumbnailUri ?: event.thumbnailResId ?: R.drawable.placeholder_poster)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.placeholder_poster),
                contentDescription = "Event Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Rating
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Format angka (misal 4.2)
                val ratingFormat = DecimalFormat("#.0")
                Text(
                    text = if (averageRating == 0.0) "N/A" else ratingFormat.format(averageRating),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                // Bintang
                Row {
                    (1..5).forEach { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index <= averageRating) Color(0xFFFFC107) else Color.LightGray,
                            modifier = Modifier.size(20.dp) // Ukuran bintang
                        )
                    }
                }
                Text(
                    text = "$totalReviews Ulasan",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun FeedbackItemCard(
    feedback: Feedback,
    onLihatFeedbackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // --- Baris Atas: Nama, Tanggal, Rating ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nama dan Tanggal
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${feedback.userName}${if (feedback.isAnda) " (Anda)" else ""}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (feedback.isAnda) PrimaryGreen else Color(0xFF1A1A1A)
                    )
                    Text(
                        text = feedback.postDate,
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
                // Rating (enhanced badge)
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFE8F5E9), // Light green background
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB300), // Gold star
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = feedback.rating.toString(),
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Teks Ulasan ---
            Text(
                text = feedback.review,
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- Baris Bawah: Lihat Feedback / Edit & Hapus ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Lihat Feedback ->"
                Row(
                    modifier = Modifier.clickable { onLihatFeedbackClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lihat Feedback",
                        color = Color(0xFF1E88E5), // Biru
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Lihat Feedback",
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Tampilkan tombol Edit/Hapus hanya jika ini ulasan user
                if (feedback.isAnda) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol Edit
                        Button(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E88E5), // Biru
                                contentColor = White
                            ),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Edit", fontSize = 12.sp)
                        }
                        // Tombol Hapus
                        Button(
                            onClick = onDeleteClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE53935), // Merah
                                contentColor = White
                            ),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Hapus", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// Dialog Hapus (sesuai UI)
@Composable
private fun DeleteFeedbackDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier.width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Apakah kamu ingin menghapus ulasan ini?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text("iya")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935), // Merah
                            contentColor = Color.White
                        )
                    ) {
                        Text("tidak")
                    }
                }
            }
        }
    }
}