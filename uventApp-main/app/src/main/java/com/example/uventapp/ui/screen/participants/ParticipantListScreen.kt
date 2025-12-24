package com.example.uventapp.ui.screen.participants

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.uventapp.data.network.ApiClient
import com.example.uventapp.data.network.GetParticipantsResponse
import com.example.uventapp.data.network.ParticipantData
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ParticipantListScreen(
    navController: NavController,
    eventId: Int,
    eventTitle: String
) {
    val context = LocalContext.current
    var participants by remember { mutableStateOf<List<ParticipantData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load participants saat screen dibuka
    LaunchedEffect(eventId) {
        ApiClient.instance.getParticipantsByEvent(eventId).enqueue(object : Callback<GetParticipantsResponse> {
            override fun onResponse(
                call: Call<GetParticipantsResponse>,
                response: Response<GetParticipantsResponse>
            ) {
                isLoading = false
                val body = response.body()
                if (response.isSuccessful && body?.status == "success") {
                    participants = body.data
                    android.util.Log.d("ParticipantList", "Loaded ${participants.size} participants")
                } else {
                    errorMessage = "Gagal memuat peserta"
                    android.util.Log.e("ParticipantList", "Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<GetParticipantsResponse>, t: Throwable) {
                isLoading = false
                errorMessage = "Gagal terhubung ke server"
                android.util.Log.e("ParticipantList", "Failure: ${t.message}")
            }
        })
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Peserta: $eventTitle",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header jumlah peserta
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Peserta",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${participants.size}",
                        fontSize = 28.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading state
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            // Error state
            else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Terjadi kesalahan",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
            // Success - tampilkan list peserta
            else if (participants.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada peserta yang mendaftar",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(participants) { participant ->
                        ParticipantCard(
                            participant = participant,
                            onViewKRS = { krsUrl ->
                                // Fix URL jika IP salah (sama seperti foto)
                                val fixedUrl = com.example.uventapp.utils.ImageUrlHelper.fixImageUrl(krsUrl)
                                if (fixedUrl != null) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl))
                                    context.startActivity(intent)
                                } else {
                                    android.util.Log.e("ParticipantList", "Invalid KRS URL: $krsUrl")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantCard(
    participant: ParticipantData,
    onViewKRS: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Nama dan NIM
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Person",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = participant.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "NIM: ${participant.nim}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fakultas & Jurusan
            Text(
                text = "${participant.fakultas} - ${participant.jurusan}",
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = participant.email,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Phone
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = participant.phone,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Lihat KRS
            if (participant.krs_uri != null) {
                Button(
                    onClick = { onViewKRS(participant.krs_uri) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = "KRS",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lihat KRS", fontSize = 14.sp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "KRS tidak tersedia",
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
            }
        }
    }
}
