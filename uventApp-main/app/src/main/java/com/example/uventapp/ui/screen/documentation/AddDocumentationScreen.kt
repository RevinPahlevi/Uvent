package com.example.uventapp.ui.screen.documentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.data.model.Documentation
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.uventapp.ui.screen.profile.ProfileViewModel

// Gradient colors
private val GradientStart = Color(0xFF00897B)
private val GradientEnd = Color(0xFF4DB6AC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentationScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    profileViewModel: ProfileViewModel,
    eventId: Int?,
    docId: Int?
) {
    val context = LocalContext.current
    
    val currentUserProfile by profileViewModel.profile
    val currentUserId = currentUserProfile?.id
    
    val event = remember(eventId, viewModel.allEvents.value, viewModel.createdEvents.value, viewModel.followedEvents) {
        (viewModel.allEvents.value + viewModel.createdEvents.value + viewModel.followedEvents).find { it.id == eventId }
    }

    val existingDoc = remember(eventId, docId, viewModel.documentations.value) {
        if (eventId != null && docId != null) {
            viewModel.getDocumentationForEvent(eventId).find { it.id == docId }
        } else {
            null
        }
    }

    var descriptionText by remember { mutableStateOf(existingDoc?.description ?: "") }
    var selectedPhotoUri by remember { mutableStateOf(existingDoc?.photoUri?.toUri()) }
    
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val isEditMode = existingDoc != null
    val appBarTitle = if (isEditMode) "Edit Dokumentasi" else "Tambah Dokumentasi"
    val buttonText = if (isEditMode) "Simpan Perubahan" else "Bagikan"

    // Gallery launcher - hanya aktif jika bukan mode edit
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (!isEditMode) {  // Hanya boleh ganti foto jika bukan edit mode
            selectedPhotoUri = uri
            uploadError = null
        }
    }

    // Fungsi untuk submit dokumentasi baru
    fun submitNewDocumentation(serverPhotoUrl: String) {
        val registrationData = viewModel.getRegistrationData(event!!.id)
        val userName = registrationData?.name ?: "Mahasiswa (Anda)"
        val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val newDoc = Documentation(
            id = (System.currentTimeMillis() % 10000).toInt(),
            eventId = event.id,
            userId = currentUserId ?: 0,
            description = descriptionText,
            photoUri = serverPhotoUrl,
            userName = userName,
            postDate = currentDate,
            postTime = currentTime,
            isAnda = true
        )
        
        val userId = if (currentUserId != null && currentUserId > 0) currentUserId else 1
        viewModel.submitDocumentation(event.id, newDoc, userId, context)
        navController.popBackStack()
    }
    
    // Fungsi untuk update dokumentasi yang sudah ada (hanya description)
    fun updateExistingDocumentation() {
        val userId = if (currentUserId != null && currentUserId > 0) currentUserId else 1
        viewModel.updateDocumentation(
            eventId = event!!.id,
            docId = existingDoc!!.id,
            newDescription = descriptionText,
            userId = userId,
            context = context
        )
        navController.popBackStack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CustomAppBar(title = appBarTitle, onBack = { navController.popBackStack() })
            },
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Header dengan gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "📸 Bagikan Momen",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Abadikan momen terbaik dari ${event.title}",
                            fontSize = 14.sp,
                            color = White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Upload Photo Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Foto Dokumentasi",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Upload Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selectedPhotoUri == null) 
                                            Color(0xFFF5F5F5) 
                                        else 
                                            Color.Transparent
                                    )
                                    .border(
                                        border = BorderStroke(
                                            width = 2.dp,
                                            brush = if (selectedPhotoUri == null)
                                                Brush.linearGradient(listOf(GradientStart, GradientEnd))
                                            else
                                                Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = !isUploading && !isEditMode) {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedPhotoUri == null) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(listOf(GradientStart.copy(0.2f), GradientEnd.copy(0.2f)))
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = null,
                                                tint = PrimaryGreen,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        Text(
                                            text = "Ketuk untuk pilih foto",
                                            color = PrimaryGreen,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Pilih dari galeri",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(selectedPhotoUri)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Foto Dipilih",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                        // Change photo button - hanya tampil jika bukan edit mode
                                        if (!isEditMode) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(8.dp)
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(Color.Black.copy(alpha = 0.6f))
                                                    .clickable {
                                                        galleryLauncher.launch(
                                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                        )
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "Ganti Foto",
                                                    color = White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Description Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✏️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Deskripsi",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = descriptionText,
                                onValueChange = { descriptionText = it },
                                placeholder = { 
                                    Text(
                                        "Ceritakan momen ini...",
                                        color = Color.Gray
                                    ) 
                                },
                                enabled = !isUploading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedContainerColor = Color(0xFFFAFAFA),
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                )
                            )
                        }
                    }

                    // Error message
                    uploadError?.let { error ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (isEditMode) {
                                // Mode edit: hanya update description
                                updateExistingDocumentation()
                            } else {
                                // Mode create: upload foto dulu lalu submit
                                if (selectedPhotoUri != null && !isUploading) {
                                    isUploading = true
                                    uploadError = null
                                    
                                    val photoUriString = selectedPhotoUri.toString()
                                    if (photoUriString.startsWith("http://") || photoUriString.startsWith("https://")) {
                                        submitNewDocumentation(photoUriString)
                                        isUploading = false
                                    } else {
                                        viewModel.uploadImage(
                                            context = context,
                                            imageUri = selectedPhotoUri!!,
                                            onSuccess = { serverUrl ->
                                                isUploading = false
                                                submitNewDocumentation(serverUrl)
                                            },
                                            onError = { error ->
                                                isUploading = false
                                                uploadError = "Gagal upload foto: $error"
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        enabled = descriptionText.isNotEmpty() && (isEditMode || selectedPhotoUri != null) && !isUploading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                color = White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Mengupload...", fontSize = 16.sp)
                        } else {
                            Icon(
                                imageVector = if (isEditMode) Icons.Filled.CheckCircle else Icons.Filled.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = buttonText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Loading overlay
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen, strokeWidth = 3.dp)
                        Text(
                            text = "Mengupload foto...",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Mohon tunggu sebentar",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}