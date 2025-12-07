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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri // <-- IMPORT BARU
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.data.model.Documentation
import com.example.uventapp.data.model.dummyEvents
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.uventapp.ui.screen.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentationScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    profileViewModel: ProfileViewModel,
    eventId: Int?,
    docId: Int? // <-- PARAMETER BARU
) {
    val context = LocalContext.current
    
    // Ambil ID user yang sedang login
    val currentUserProfile by profileViewModel.profile
    val currentUserId = currentUserProfile?.id
    
    val event = remember(eventId, viewModel.createdEvents.value, dummyEvents, viewModel.followedEvents) {
        (dummyEvents + viewModel.createdEvents.value + viewModel.followedEvents).find { it.id == eventId }
    }

    // --- PERBAIKAN: Cari dokumen yang ada jika ini mode Edit ---
    val existingDoc = remember(eventId, docId, viewModel.documentations.value) {
        if (eventId != null && docId != null) {
            viewModel.getDocumentationForEvent(eventId).find { it.id == docId }
        } else {
            null
        }
    }

    // --- PERBAIKAN: Isi state dengan data yang ada ---
    var descriptionText by remember { mutableStateOf(existingDoc?.description ?: "") }
    var selectedPhotoUri by remember { mutableStateOf(existingDoc?.photoUri?.toUri()) } // Konversi String ke Uri

    // Tentukan mode (Tambah/Edit)
    val isEditMode = existingDoc != null
    val appBarTitle = if (isEditMode) "Edit Dokumentasi" else "Tambah Dokumentasi"
    val buttonText = if (isEditMode) "Simpan Perubahan" else "Post"

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        selectedPhotoUri = uri
    }

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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            DocumentationCard(title = "Upload Foto") {
                UploadFotoBox(
                    photoUri = selectedPhotoUri,
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            DocumentationCard(title = "Deskripsi") {
                TextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    placeholder = { Text("Tambahkan deskripsi foto...") },
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

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = buttonText, // Teks tombol dinamis
                onClick = {
                    val registrationData = viewModel.getRegistrationData(event.id)
                    val userName = registrationData?.name ?: "Mahasiswa (Anda)"

                    // --- PERBAIKAN: Gunakan data lama atau data baru ---
                    val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
                    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                    val newDoc = Documentation(
                        // Gunakan ID lama jika edit, buat ID baru jika tambah
                        id = existingDoc?.id ?: (System.currentTimeMillis() % 10000).toInt(),
                        eventId = event.id,
                        description = descriptionText,
                        photoUri = selectedPhotoUri?.toString(),
                        userName = userName,
                        // Gunakan tanggal/waktu posting lama jika edit, buat baru jika tambah
                        postDate = existingDoc?.postDate ?: currentDate,
                        postTime = existingDoc?.postTime ?: currentTime,
                        isAnda = true
                    )
                    
                    // Kirim dokumentasi ke API dengan userId dan context
                    if (currentUserId != null && currentUserId > 0) {
                        viewModel.submitDocumentation(event.id, newDoc, currentUserId, context)
                    } else {
                        // Fallback: Gunakan ID 1 untuk testing jika belum login
                        viewModel.submitDocumentation(event.id, newDoc, 1, context)
                    }

                    // Kembali ke layar AllDocumentation
                    navController.navigate(Screen.AllDocumentation.createRoute(event.id)) {
                        popUpTo(Screen.AllDocumentation.createRoute(event.id)) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                enabled = descriptionText.isNotEmpty() && selectedPhotoUri != null
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- Helper Composables (Mirip AddFeedbackScreen) ---

@Composable
private fun DocumentationCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun UploadFotoBox(
    photoUri: Uri?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LightBackground)
            .clickable { onClick() }
            .border(
                border = BorderStroke(1.dp, PrimaryGreen),
                shape = RoundedCornerShape(8.dp)
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
                contentDescription = "Foto Dipilih",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}