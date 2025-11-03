package com.example.uventapp.ui.screen.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.uventapp.R
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.repository.EventRepository
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White

@Composable
fun DetailEventScreen(navController: NavController, eventId: String?) {
    val event = EventRepository.events.find { it.id == eventId }

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

                        val posterRes = when (event.id) {
                            "1" -> R.drawable.event_talkshow
                            "2" -> R.drawable.event_seminar
                            "3" -> R.drawable.event_skill_lab
                            else -> R.drawable.placeholder_poster
                        }

                        Image(
                            painter = painterResource(id = posterRes),
                            contentDescription = "Poster Event",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        EventDetailTable(event)

                        Spacer(modifier = Modifier.height(24.dp))

                        PrimaryButton(
                            text = "Daftar Sekarang",
                            onClick = {
                                // Navigasi ke RegistrationFormScreen (form pendaftaran event)
                                navController.navigate(Screen.RegistrationFormScreen.route)
                            }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Event tidak ditemukan",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Kembali",
                        onClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun EventDetailTable(event: Event) {
    val details = listOf(
        "Judul Event" to event.title,
        "Jenis Event" to event.type,
        "Tanggal" to event.date,
        "Waktu" to event.time,
        "Lokasi" to event.location,
        "Kuota" to event.quota.toString(),
        "Status" to event.status
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.width(130.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            details.forEach { (label, _) ->
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PrimaryGreen
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            details.forEach { (_, value) ->
                Row {
                    Text(
                        text = ": ",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
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
