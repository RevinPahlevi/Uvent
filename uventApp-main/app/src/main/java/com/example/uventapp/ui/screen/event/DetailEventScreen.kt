package com.example.uventapp.ui.screen.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Import Coil
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage // Import Coil
import coil.request.ImageRequest // Import Coil
import com.example.uventapp.R
// PERBAIKAN: Impor Event dan dummyEvents yang benar
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.model.dummyEvents
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
// TAMBAHKAN: Import ViewModel
import com.example.uventapp.ui.screen.event.EventManagementViewModel

@Composable
fun DetailEventScreen(
    navController: NavController,
    eventId: Int?,
    viewModel: EventManagementViewModel // PERBAIKAN: Terima ViewModel
) {

    // PERBAIKAN: Cari event dari gabungan dummyEvents dan event buatan di ViewModel
    val event = remember(eventId, viewModel.createdEvents.value) {
        (dummyEvents + viewModel.createdEvents.value).find { it.id == eventId }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Detail Event",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // PERBAIKAN: Cek jika event ditemukan
            if (event != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // --- PERUBAHAN UNTUK REQUEST 2 (URI) ---
                        // Banner Event (Gunakan AsyncImage)
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(event.thumbnailUri ?: event.thumbnailResId ?: R.drawable.placeholder_poster)
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.placeholder_poster),
                            contentDescription = "Event Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp) // Beri tinggi tetap
                                .clip(RoundedCornerShape(8.dp))
                        )
                        // ---------------------------------------

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tabel Detail Event
                        EventDetailTable(event = event) // Gunakan data dari event yang ditemukan

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tombol Daftar Sekarang
                        PrimaryButton(
                            text = "Daftar Sekarang",
                            onClick = {
                                // --- PERBAIKAN ERROR ---
                                // Navigasi menggunakan event.id (Int) sesuai perbaikan di Screen.kt
                                navController.navigate(Screen.RegistrationFormScreen.createRoute(event.id))
                            },
                        )
                    }
                }
            } else {
                // Tampilan jika event tidak ditemukan
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Event tidak ditemukan.")
                }
            }
        }
    }
}

@Composable
fun EventDetailTable(event: Event) { // Tipe 'Event' sekarang jelas
    val details = listOf(
        "Judul Event" to event.title,
        "Jenis Event" to event.type,
        "Tanggal" to event.date,
        "Waktu" to event.time,
        "Lokasi/Platform" to event.location,
        "Kuota" to event.quota, // Kuota sudah String di model baru
        "Status" to event.status
    )

    // Header (kolom kiri) dan Nilai (kolom kanan)
    Row(modifier = Modifier.fillMaxWidth()) {
        // Kolom Label
        Column(modifier = Modifier.width(120.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            details.forEach { (label, _) ->
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PrimaryGreen
                )
            }
        }

        // Kolom Nilai
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            details.forEach { (_, value) ->
                Row {
                    Text(text = ": ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}