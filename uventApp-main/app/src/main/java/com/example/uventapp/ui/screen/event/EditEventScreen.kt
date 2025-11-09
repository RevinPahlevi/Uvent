package com.example.uventapp.ui.screen.event

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.uventapp.R
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.theme.DisabledBackground
import com.example.uventapp.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    eventId: Int?
) {
    // 1. Ambil data event berdasarkan ID
    val eventToEdit = remember(eventId) {
        eventId?.let { viewModel.getEventById(it) }
    }

    // Tampilkan pesan jika event tidak ditemukan
    if (eventToEdit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Event tidak ditemukan atau ID salah.")
        }
        return
    }

    // 2. Isi state form dengan data yang sudah ada
    var judul by remember { mutableStateOf(eventToEdit.title) }
    var jenis by remember { mutableStateOf(eventToEdit.type) }
    var tanggal by remember { mutableStateOf(eventToEdit.date) }
    var waktu by remember { mutableStateOf(eventToEdit.time) }
    var lokasi by remember { mutableStateOf(eventToEdit.location) }
    var kuota by remember { mutableStateOf(eventToEdit.quota) }
    var posterUri by remember { mutableStateOf<Uri?>(null) } // State untuk poster baru

    // Launcher untuk memilih gambar dari galeri
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        posterUri = uri
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Edit Event",
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Composable untuk Upload Poster (versi Edit)
            UploadPosterField(
                isEditing = true,
                posterUri = posterUri,
                onClick = { imagePickerLauncher.launch("image/*") } // Membuka galeri
            )

            // Input Fields
            EventInputField(
                label = "Judul Event",
                value = judul,
                onValueChange = { judul = it }
            )
            EventInputField(
                label = "Jenis Event",
                value = jenis,
                onValueChange = { jenis = it }
            )
            EventInputField(
                label = "Tanggal Event",
                value = tanggal,
                onValueChange = { tanggal = it },
                placeholder = "DD/MM/YYYY",
                trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Kalender") }
            )
            EventInputField(
                label = "Waktu",
                value = waktu,
                placeholder = "09:00 - 12:00",
                onValueChange = { waktu = it }
            )
            EventInputField(
                label = "Lokasi/Platform",
                value = lokasi,
                onValueChange = { lokasi = it }
            )
            EventInputField(
                label = "Kuota",
                value = kuota,
                onValueChange = { kuota = it },
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Tombol Simpan Perubahan
            PrimaryButton(text = "Simpan Perubahan", onClick = {
                val updatedEvent = eventToEdit.copy(
                    title = judul,
                    type = jenis,
                    date = tanggal,
                    time = waktu,
                    location = lokasi,
                    quota = kuota,
                    // TODO: Ganti thumbnail jika posterUri tidak null
                )

                viewModel.updateEvent(updatedEvent)
                navController.popBackStack() // Kembali ke MyEventsScreen (dan memicu notifikasi)
            })

            Spacer(modifier = Modifier.height(16.dp)) // Padding di bawah tombol
        }
    }
}

/**
 * Composable kustom untuk field input form
 * (Didefinisikan ulang di sini agar file tetap mandiri)
 */
@Composable
private fun EventInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            placeholder = { if (placeholder != null) Text(placeholder, color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DisabledBackground,
                unfocusedContainerColor = DisabledBackground,
                disabledContainerColor = DisabledBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PrimaryGreen
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = trailingIcon,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Composable untuk area upload poster (versi Edit)
 * (Didefinisikan ulang di sini agar file tetap mandiri)
 */
@Composable
private fun UploadPosterField(
    isEditing: Boolean, // Tambahan parameter
    posterUri: Uri?,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Poster Event", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))

        // Area dashed border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(
                    width = 2.dp,
                    color = PrimaryGreen,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onClick)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Tampilan teks berdasarkan mode (Edit/Tambah) dan apakah poster baru dipilih
            if (posterUri == null) {
                // Tampilan default (baik Tambah atau Edit sebelum poster baru dipilih)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (isEditing) Icons.Filled.Edit else Icons.Filled.Edit, // Menggunakan ikon yang sama
                        contentDescription = "Upload",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = if (isEditing) "Ganti Poster" else "+ Upload Poster",
                        color = PrimaryGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Klik untuk pilih dari galeri",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } else {
                // Tampilan setelah poster baru dipilih
                Text(
                    text = "Gambar Baru Dipilih: ${posterUri.lastPathSegment}",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
