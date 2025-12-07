package com.example.uventapp.ui.screen.feedback

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest // Import baru
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.uventapp.data.model.dummyEvents
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.uventapp.ui.screen.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedbackScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    profileViewModel: ProfileViewModel,
    eventId: Int?
) {
    val context = LocalContext.current
    
    // Ambil ID user yang sedang login
    val currentUserProfile by profileViewModel.profile
    val currentUserId = currentUserProfile?.id
    
    val event = remember(eventId, viewModel.createdEvents.value, dummyEvents, viewModel.followedEvents) {
        (dummyEvents + viewModel.createdEvents.value + viewModel.followedEvents).find { it.id == eventId }
    }

    // PERBAIKAN: Ambil data feedback yang *mungkin* sudah ada
    val existingFeedback = remember(eventId, viewModel.feedbacks.value) {
        viewModel.getFeedbacksForEvent(eventId ?: -1).find { it.isAnda }
    }

    var rating by remember { mutableStateOf(existingFeedback?.rating ?: 0) }
    var reviewText by remember { mutableStateOf(existingFeedback?.review ?: "") }
    // PERBAIKAN: Safe URI parsing dengan try-catch
    var selectedPhotoUri by remember { 
        mutableStateOf(
            existingFeedback?.photoUri?.let { uriString ->
                try {
                    Uri.parse(uriString)
                } catch (e: Exception) {
                    null
                }
            }
        ) 
    }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // --- PERBAIKAN 1: Logika Pemilih Sumber Gambar ---
    var showSourceDialog by remember { mutableStateOf(false) }

    // Launcher 1: Galeri (menggunakan PickVisualMedia)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedPhotoUri = uri
        showSourceDialog = false
    }

    // Launcher 2: Kamera (menggunakan TakePicture)
    // TODO: Ini memerlukan setup FileProvider dan izin kamera di AndroidManifest.xml
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            // Dapatkan URI dari file yang disimpan
            // selectedPhotoUri = ... (ini memerlukan URI yang disimpan)
        }
        showSourceDialog = false
    }

    // Tampilkan dialog pilihan
    if (showSourceDialog) {
        SourceOptionDialog(
            onDismiss = { showSourceDialog = false },
            onCameraClick = {
                // TODO: Minta izin kamera sebelum meluncurkan
                // cameraLauncher.launch(uriUntukFileBaru)

                // --- UNTUK SAAT INI, KITA LANGSUNG BUKA GALERI ---
                // (Karena setup kamera butuh FileProvider & Izin Manifest)
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onGalleryClick = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
    // ---------------------------------------------

    Scaffold(
        topBar = {
            CustomAppBar(title = "Feedback", onBack = { navController.popBackStack() })
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EventInfoCard(event = event)
            FeedbackCard(title = "Berikan Rating") {
                RatingBar(
                    rating = rating,
                    onRatingChanged = { newRating -> rating = newRating }
                )
            }
            FeedbackCard(title = "Berikan Ulasanmu") {
                TextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Ceritakan pengalamanmu di event ini.......") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LightBackground,
                        unfocusedContainerColor = LightBackground,
                        disabledContainerColor = LightBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }
            FeedbackCard(title = "Upload Foto Suasana Event") {
                UploadFotoBox(
                    photoUri = selectedPhotoUri,
                    onClick = {
                        showSourceDialog = true
                    }
                )
            }
            PrimaryButton(
                text = "Kirim Ulasan",
                onClick = { showConfirmationDialog = true },
                enabled = rating > 0 && reviewText.isNotEmpty()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showConfirmationDialog) {
            ConfirmationDialog(
                onDismiss = { showConfirmationDialog = false },
                onConfirm = {
                    // --- PERBAIKAN 2: Ambil nama dari data registrasi ---
                    val registrationData = viewModel.getRegistrationData(event.id)
                    val userName = registrationData?.name ?: currentUserProfile?.name ?: "Mahasiswa (Anda)"
                    // ------------------------------------------------

                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())

                    val feedback = Feedback(
                        // Jika feedback sudah ada, gunakan ID lama (untuk edit)
                        // Jika baru, buat ID unik sementara
                        id = existingFeedback?.id ?: (System.currentTimeMillis() % 10000).toInt(),
                        eventId = event.id,
                        rating = rating,
                        review = reviewText,
                        photoUri = selectedPhotoUri?.toString(),
                        userName = userName,
                        postDate = currentDate, // Tanggal hari ini
                        isAnda = true // Tandai sebagai milik user
                    )
                    
                    // Debug log
                    android.util.Log.d("AddFeedbackScreen", "=== KIRIM ULASAN ===")
                    android.util.Log.d("AddFeedbackScreen", "event.id: ${event.id}")
                    android.util.Log.d("AddFeedbackScreen", "currentUserId: $currentUserId")
                    android.util.Log.d("AddFeedbackScreen", "rating: $rating")
                    android.util.Log.d("AddFeedbackScreen", "review: $reviewText")
                    
                    // Kirim feedback ke API dengan userId dan context
                    if (currentUserId != null && currentUserId > 0) {
                        viewModel.submitFeedback(event.id, feedback, currentUserId, context)
                    } else {
                        android.util.Log.e("AddFeedbackScreen", "ERROR: User ID tidak valid: $currentUserId")
                        // Fallback: Gunakan ID 1 untuk testing jika belum login
                        viewModel.submitFeedback(event.id, feedback, 1, context)
                    }
                    showConfirmationDialog = false
                    navController.popBackStack()
                }
            )
        }
    }
}

// --- Dialog Pilihan Sumber Gambar ---
@Composable
private fun SourceOptionDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Sumber Foto") },
        text = { Text("Ambil foto dari kamera atau galeri?") },
        confirmButton = {
            TextButton(onClick = onCameraClick) {
                Text("Kamera")
            }
        },
        dismissButton = {
            TextButton(onClick = onGalleryClick) {
                Text("Galeri")
            }
        }
    )
}

@Composable
private fun FeedbackCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF2E7D32) // Dark green for titles
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun EventInfoCard(event: Event) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.thumbnailUri ?: event.thumbnailResId ?: R.drawable.placeholder_poster)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.placeholder_poster),
                contentDescription = "Event Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(event.type, fontSize = 13.sp, color = PrimaryGreen, modifier = Modifier.padding(bottom = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CalendarToday,
                        contentDescription = "Tanggal",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.date, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        (1..5).forEach { index ->
            Icon(
                imageVector = if (index <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Rating $index",
                tint = if (index <= rating) Color(0xFFFFB300) else Color(0xFFE0E0E0), // Vibrant gold / light gray
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onRatingChanged(index)
                    }
            )
            if (index < 5) {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

@Composable
private fun UploadFotoBox(
    photoUri: Uri?,
    onClick: () -> Unit
) {
    val stroke = Stroke(
        width = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LightBackground)
            .clickable { onClick() }
            .then(
                if (photoUri == null) {
                    Modifier.border(
                        border = BorderStroke(1.dp, PrimaryGreen),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Upload Foto",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Upload Foto",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto Event",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ConfirmationDialog(
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
                    text = "Apakah kamu ingin mengirim ulasan ini?",
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
                            containerColor = Color(0xFFE53935), // Warna merah
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