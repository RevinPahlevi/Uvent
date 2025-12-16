package com.example.uventapp.ui.screen.feedback

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.R
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.model.Feedback
import com.example.uventapp.data.model.dummyEvents
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.ui.screen.profile.ProfileViewModel
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedbackScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    profileViewModel: ProfileViewModel,
    eventId: Int?
) {
    val context = LocalContext.current
    
    val currentUserProfile by profileViewModel.profile
    val currentUserId = currentUserProfile?.id

    val allEvents by viewModel.allEvents
    
    val event = remember(eventId, allEvents, viewModel.createdEvents.value, dummyEvents, viewModel.followedEvents) {
        allEvents.find { it.id == eventId }
            ?: viewModel.createdEvents.value.find { it.id == eventId }
            ?: viewModel.followedEvents.find { it.id == eventId }
            ?: dummyEvents.find { it.id == eventId }
    }

    val isEventCreator = remember(event, currentUserId) {
        event?.creatorId != null && event.creatorId == currentUserId
    }

    LaunchedEffect(isEventCreator, event) {
        if (isEventCreator && event != null) {
            Toast.makeText(
                context,
                "Pembuat event tidak dapat memberikan ulasan untuk event sendiri",
                Toast.LENGTH_LONG
            ).show()
            navController.popBackStack()
        }
    }

    val existingFeedback = remember(eventId, viewModel.feedbacks.value) {
        viewModel.getFeedbacksForEvent(eventId ?: -1).find { it.isAnda }
    }

    var rating by remember { mutableStateOf(existingFeedback?.rating ?: 0) }
    var reviewText by remember { mutableStateOf(existingFeedback?.review ?: "") }
    var selectedPhotoUri by remember { 
        mutableStateOf(
            existingFeedback?.photoUri?.let { uriString ->
                try { Uri.parse(uriString) } catch (e: Exception) { null }
            }
        ) 
    }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showSourceDialog by remember { mutableStateOf(false) }
    
    // Camera URI state
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
        showSourceDialog = false
    }

    // Camera launcher - takes picture and saves to URI
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            selectedPhotoUri = tempCameraUri
        }
        showSourceDialog = false
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Create temp file for camera
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = File(context.cacheDir, "images")
            if (!storageDir.exists()) storageDir.mkdirs()
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            tempCameraUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            tempCameraUri?.let { cameraLauncher.launch(it) }
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to launch camera
    fun launchCamera() {
        val permission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, create file and launch camera
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "JPEG_${timeStamp}_"
                val storageDir = File(context.cacheDir, "images")
                if (!storageDir.exists()) storageDir.mkdirs()
                val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
                tempCameraUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
                tempCameraUri?.let { cameraLauncher.launch(it) }
            }
            else -> {
                // Request permission
                cameraPermissionLauncher.launch(permission)
            }
        }
    }

    if (showSourceDialog) {
        SourceOptionDialog(
            onDismiss = { showSourceDialog = false },
            onCameraClick = {
                showSourceDialog = false
                launchCamera()
            },
            onGalleryClick = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }

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
            Spacer(modifier = Modifier.height(8.dp))
            EventInfoCard(event = event)
            FeedbackCard(title = "Berikan Rating", icon = Icons.Filled.Star) {
                ModernRatingBar(
                    rating = rating,
                    onRatingChanged = { newRating -> rating = newRating }
                )
            }
            FeedbackCard(title = "Berikan Ulasanmu", icon = Icons.Filled.Edit) {
                TextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Ceritakan pengalamanmu di event ini.......", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
                // Character counter
                Text(
                    text = "${reviewText.length}/500",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                )
            }
            FeedbackCard(title = "Upload Foto Suasana Event", icon = Icons.Filled.CameraAlt) {
                UploadFotoBox(
                    photoUri = selectedPhotoUri,
                    onClick = { showSourceDialog = true }
                )
            }
            
            // Modern Submit Button
            GradientSubmitButton(
                text = if (existingFeedback != null) "Update Ulasan" else "Kirim Ulasan",
                enabled = rating > 0 && reviewText.isNotEmpty(),
                onClick = { showConfirmationDialog = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Confirmation Dialog
        if (showConfirmationDialog) {
            ModernConfirmationDialog(
                onDismiss = { showConfirmationDialog = false },
                onConfirm = {
                    val registrationData = viewModel.getRegistrationData(event.id)
                    val userName = registrationData?.name ?: currentUserProfile?.name ?: "Mahasiswa (Anda)"

                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())

                    val feedback = Feedback(
                        id = existingFeedback?.id ?: (System.currentTimeMillis() % 10000).toInt(),
                        eventId = event.id,
                        rating = rating,
                        review = reviewText,
                        photoUri = selectedPhotoUri?.toString(),
                        userName = userName,
                        postDate = currentDate,
                        isAnda = true
                    )
                    
                    val isEdit = existingFeedback != null
                    val feedbackIdForEdit = existingFeedback?.id
                    
                    if (currentUserId != null && currentUserId > 0) {
                        viewModel.submitFeedback(event.id, feedback, currentUserId, context, isEdit, feedbackIdForEdit)
                    } else {
                        viewModel.submitFeedback(event.id, feedback, 1, context, isEdit, feedbackIdForEdit)
                    }
                    showConfirmationDialog = false
                    showSuccessDialog = true
                }
            )
        }

        // Success Dialog
        if (showSuccessDialog) {
            SuccessDialog(
                isEdit = existingFeedback != null,
                onDismiss = {
                    showSuccessDialog = false
                    navController.popBackStack()
                }
            )
        }
    }
}

// --- Modern Rating Bar with Animation ---
@Composable
private fun ModernRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { index ->
            val scale by animateFloatAsState(
                targetValue = if (index <= rating) 1.2f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "scale"
            )
            val color by animateColorAsState(
                targetValue = if (index <= rating) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                animationSpec = tween(200),
                label = "color"
            )
            
            Icon(
                imageVector = if (index <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Star $index",
                tint = color,
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onRatingChanged(index) }
                    .padding(4.dp)
            )
        }
    }
    
    // Rating text
    val ratingText = when (rating) {
        1 -> "Buruk 😞"
        2 -> "Kurang 😕"
        3 -> "Cukup 😐"
        4 -> "Bagus 😊"
        5 -> "Sangat Bagus 🎉"
        else -> "Ketuk bintang untuk memberi rating"
    }
    
    Text(
        text = ratingText,
        fontSize = 14.sp,
        color = if (rating > 0) PrimaryGreen else Color.Gray,
        fontWeight = if (rating > 0) FontWeight.Medium else FontWeight.Normal,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        textAlign = TextAlign.Center
    )
}

// --- Gradient Submit Button ---
@Suppress("DEPRECATION")
@Composable
private fun GradientSubmitButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = if (enabled) 
                    Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF43A047)))
                else 
                    Brush.horizontalGradient(listOf(Color(0xFFBDBDBD), Color(0xFFBDBDBD)))
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Send,
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

// --- Source Option Dialog ---
@Composable
private fun SourceOptionDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pilih Sumber Foto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Camera option
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCameraClick() }
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFFE3F2FD), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = "Kamera",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kamera", fontSize = 14.sp)
                    }
                    
                    // Gallery option
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onGalleryClick() }
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFFF3E5F5), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PhotoLibrary,
                                contentDescription = "Galeri",
                                tint = Color(0xFF7B1FA2),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Galeri", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// --- Feedback Card with Icon ---
@Composable
private fun FeedbackCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2E7D32)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

// --- Event Info Card ---
@Composable
private fun EventInfoCard(event: Event) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.date,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// --- Upload Foto Box ---
@Composable
private fun UploadFotoBox(
    photoUri: Uri?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF5F5F5))
            .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CloudUpload,
                        contentDescription = "Upload",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ketuk untuk upload foto",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// --- Modern Confirmation Dialog ---
@Composable
private fun ModernConfirmationDialog(
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
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.RateReview,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Kirim Ulasan?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pastikan ulasan kamu sudah sesuai sebelum mengirim.",
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
                            containerColor = PrimaryGreen,
                            contentColor = White
                        )
                    ) {
                        Text("Kirim", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// --- Beautiful Success Dialog with Animation ---
@Composable
private fun SuccessDialog(
    isEdit: Boolean,
    onDismiss: () -> Unit
) {
    // Animation states
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val sparkleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Auto dismiss after 2 seconds
    LaunchedEffect(Unit) {
        delay(2500)
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier
                .width(340.dp)
                .scale(scale)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success icon with gradient background
                Box(contentAlignment = Alignment.Center) {
                    // Sparkle effect
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .rotate(sparkleRotation)
                            .background(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0x1043A047),
                                        Color(0x3043A047),
                                        Color(0x1043A047),
                                        Color(0x3043A047)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    // Icon circle
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF43A047), Color(0xFF2E7D32))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = if (isEdit) "Ulasan Diperbarui!" else "Ulasan Terkirim!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle
                Text(
                    text = if (isEdit) 
                        "Ulasan kamu berhasil diperbarui.\nTerima kasih atas feedback-nya! 🎉" 
                    else 
                        "Terima kasih sudah memberikan ulasan.\nFeedback kamu sangat berarti! 🌟",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = White
                    )
                ) {
                    Icon(
                        Icons.Filled.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keren, Lanjutkan!", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}