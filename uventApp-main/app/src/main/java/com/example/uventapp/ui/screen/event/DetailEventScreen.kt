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
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.model.dummyEvents
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
import com.example.uventapp.ui.screen.event.EventManagementViewModel

@Composable
fun DetailEventScreen(
    navController: NavController,
    eventId: Int?,
    viewModel: EventManagementViewModel
) {
    val event = remember(eventId, viewModel.createdEvents.value, dummyEvents) {
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
            if (event != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        EventDetailTable(event = event) // <-- PERBAIKAN DI SINI

                        Spacer(modifier = Modifier.height(24.dp))

                        PrimaryButton(
                            text = "Daftar Sekarang",
                            onClick = {
                                navController.navigate(Screen.RegistrationFormScreen.createRoute(event.id))
                            },
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Event tidak ditemukan.")
                }
            }
        }
    }
}

@Composable
fun EventDetailTable(event: Event) {
    // --- PERBAIKAN: Perbarui daftar detail ---
    val details = listOf(
        "Judul Event" to event.title,
        "Jenis Event" to event.type,
        "Tanggal" to event.date,
        "Waktu Mulai" to event.timeStart,
        "Waktu Selesai" to event.timeEnd,
        "Tipe Lokasi" to event.platformType, // <-- BARU
        "Lokasi/Link" to event.locationDetail, // <-- BARU
        "Kuota" to event.quota,
        "Status" to event.status
    )
    // ----------------------------------------

    Row(modifier = Modifier.fillMaxWidth()) {
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