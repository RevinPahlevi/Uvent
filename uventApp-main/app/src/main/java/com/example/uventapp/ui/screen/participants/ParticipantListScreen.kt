package com.example.uventapp.ui.screen.participants

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.R
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.model.dummyEvents
import com.example.uventapp.data.network.ApiClient
import com.example.uventapp.data.network.GetParticipantsResponse
import com.example.uventapp.data.network.ParticipantData
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.utils.ImageUrlHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun ParticipantListScreen(
    navController: NavController,
    eventId: Int,
    eventTitle: String,
    viewModel: EventManagementViewModel
) {
    val context = LocalContext.current
    var participants by remember { mutableStateOf<List<ParticipantData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Get event data
    val allEvents by viewModel.allEvents
    val event = remember(eventId, allEvents) {
        allEvents.find { it.id == eventId }
            ?: viewModel.createdEvents.value.find { it.id == eventId }
            ?: dummyEvents.find { it.id == eventId }
    }

    // Load events dan participants saat screen dibuka
    LaunchedEffect(eventId) {
        viewModel.loadAllEvents(context)
        
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
                title = "Detail Event",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Event Detail Card
            item {
                if (event != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Event Image
                            val imageSource = ImageUrlHelper.fixImageUrl(event.thumbnailUri)
                                ?: event.thumbnailResId
                                ?: R.drawable.placeholder_poster
                            
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageSource)
                                    .crossfade(true)
                                    .build(),
                                placeholder = painterResource(R.drawable.placeholder_poster),
                                error = painterResource(R.drawable.placeholder_poster),
                                contentDescription = "Event Poster",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Event Title
                            Text(
                                text = event.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            
                            // Event Type
                            Text(
                                text = event.type,
                                fontSize = 14.sp,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Date & Time
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${event.date} • ${event.timeStart} - ${event.timeEnd}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Location
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${event.platformType} • ${event.locationDetail}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            
            // Participants Header
            item {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.People,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Peserta Terdaftar",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "${participants.size}",
                            fontSize = 28.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Loading state
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
            }
            // Error state
            else if (errorMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Terjadi kesalahan",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            // Empty state
            else if (participants.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada peserta yang mendaftar",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } 
            // Success - tampilkan list peserta
            else {
                items(participants) { participant ->
                    ParticipantCard(
                        participant = participant,
                        onViewKRS = { krsUrl ->
                            // Buka KRS di browser atau PDF viewer
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(krsUrl))
                            context.startActivity(intent)
                        }
                    )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Person",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Nama dan Jurusan
            Column {
                Text(
                    text = participant.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = participant.jurusan,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}