package com.example.uventapp.ui.screen.event

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// Import yang diperlukan untuk Coil (AsyncImage)
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.R
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.data.model.Event
import com.example.uventapp.ui.theme.LightBackground

@Composable
fun AddEventScreen(navController: NavController, viewModel: EventManagementViewModel) {
    // State untuk menyimpan data form
    var judul by remember { mutableStateOf("") }
    var jenis by remember { mutableStateOf("") }
    var tanggal by remember { mutableStateOf("") }
    var waktu by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf("") }
    var kuota by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher untuk memilih gambar dari galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            CustomAppBar(title = "Tambah Event Baru", onBack = { navController.popBackStack() })
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Poster Event (Upload)
            Text("Poster Event", fontWeight = FontWeight.SemiBold)
            PosterUploadBox(
                imageUri = imageUri,
                onClick = { galleryLauncher.launch("image/*") }
            )

            // 2. Input Fields
            FormInputTextField(label = "Judul Event", value = judul, onValueChange = { judul = it })
            FormInputTextField(label = "Jenis Event", value = jenis, onValueChange = { jenis = it })
            FormInputTextField(
                label = "Tanggal Event",
                value = tanggal,
                onValueChange = { tanggal = it },
                placeholder = "DD/MM/YYYY",
                trailingIcon = { Icon(Icons.Filled.CalendarToday, "Kalender") }
            )
            FormInputTextField(label = "Waktu", value = waktu, onValueChange = { waktu = it }, placeholder = "09:00 - 12:00")
            FormInputTextField(label = "Lokasi/Platform", value = lokasi, onValueChange = { lokasi = it })
            FormInputTextField(label = "Kuota", value = kuota, onValueChange = { kuota = it })

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Tombol Simpan
            PrimaryButton(text = "Simpan Event", onClick = {
                // Buat ID unik sementara (dalam aplikasi nyata, ini dari database)
                val newId = (viewModel.createdEvents.value.maxOfOrNull { it.id } ?: 0) + 1

                // Buat objek Event baru
                val newEvent = Event(
                    id = newId,
                    title = judul,
                    type = jenis,
                    date = tanggal,
                    time = waktu,
                    location = lokasi,
                    quota = kuota,
                    status = "Baru", // Status default
                    // Jika ada gambar, gunakan. Jika tidak, pakai placeholder
                    // (Logika ini perlu disesuaikan jika gambar disimpan di server)
                    thumbnailResId = R.drawable.placeholder_poster
                )

                viewModel.addEvent(newEvent)
                navController.popBackStack() // Kembali ke MyEventsScreen
            })
        }
    }
}

@Composable
private fun PosterUploadBox(imageUri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                BorderStroke(2.dp, Color.LightGray),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            // Tampilan placeholder jika belum ada gambar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.AddPhotoAlternate,
                    contentDescription = "Upload Poster",
                    tint = Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "+ Upload Poster",
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Klik untuk pilih dari galeri",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        } else {
            // Tampilkan gambar yang dipilih
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Poster Event",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun FormInputTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (placeholder != null) Text(placeholder) },
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

