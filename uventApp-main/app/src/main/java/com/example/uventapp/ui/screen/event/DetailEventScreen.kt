package com.example.uventapp.ui.screen.event

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.R
import com.example.uventapp.data.model.Event

import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
import com.example.uventapp.utils.ImageUrlHelper
import com.example.uventapp.utils.EventTimeHelper
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun isEventFinished(date: String, timeEnd: String): Boolean {
    try {
        val dateParts = date.split("/")
        if (dateParts.size != 3) return false
        
        val day = dateParts[0].toIntOrNull() ?: return false
        val month = dateParts[1].toIntOrNull() ?: return false
        val year = dateParts[2].toIntOrNull() ?: return false

        val timeParts = timeEnd.split(":")
        if (timeParts.size < 2) return false
        
        val hour = timeParts[0].toIntOrNull() ?: return false
        val minute = timeParts[1].toIntOrNull() ?: return false

        val eventEndCalendar = java.util.Calendar.getInstance()
        eventEndCalendar.set(java.util.Calendar.YEAR, year)
        eventEndCalendar.set(java.util.Calendar.MONTH, month - 1) // Calendar month is 0-indexed
        eventEndCalendar.set(java.util.Calendar.DAY_OF_MONTH, day)
        eventEndCalendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
        eventEndCalendar.set(java.util.Calendar.MINUTE, minute)
        eventEndCalendar.set(java.util.Calendar.SECOND, 0)
        eventEndCalendar.set(java.util.Calendar.MILLISECOND, 0)

        val now = java.util.Calendar.getInstance()

        return now.after(eventEndCalendar)
    } catch (e: Exception) {
        return false
    }
}

@Composable
fun DetailEventScreen(
    navController: NavController,
    eventId: Int?,
    viewModel: EventManagementViewModel,
    profileViewModel: com.example.uventapp.ui.screen.profile.ProfileViewModel
) {
    val context = LocalContext.current

    val currentUserProfile by profileViewModel.profile
    val currentUserId = currentUserProfile?.id

    val allEvents by viewModel.allEvents
    val createdEvents by viewModel.createdEvents
    val followedEvents = viewModel.followedEvents

    var registrationCount by remember { mutableIntStateOf(0) }
    var isLoadingCount by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadAllEvents(context)
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null && currentUserId > 0) {
            android.util.Log.d("DetailEventScreen", "Loading followed events for user: $currentUserId")
            viewModel.loadFollowedEvents(currentUserId, context)
        } else {
            android.util.Log.w("DetailEventScreen", "No valid userId, skipping loadFollowedEvents")
        }
    }

    LaunchedEffect(eventId) {
        if (eventId != null) {
            try {
                val response = com.example.uventapp.data.network.ApiClient.instance.getRegistrationCount(eventId)
                if (response.isSuccessful && response.body()?.status == "success") {
                    registrationCount = response.body()?.count ?: 0
                    android.util.Log.d("DetailEventScreen", "Registration count for event $eventId: $registrationCount")
                }
            } catch (e: Exception) {
                android.util.Log.e("DetailEventScreen", "Error loading registration count: ${e.message}")
            } finally {
                isLoadingCount = false
            }
        }
    }

    val event = remember(eventId, allEvents, createdEvents, followedEvents) {
        eventId?.let { id ->
            viewModel.allEvents.value.find { it.id == id }
                ?: viewModel.createdEvents.value.find { it.id == id }
                ?: viewModel.followedEvents.find { it.id == id }
        }
    }
    
    val isRegistered = followedEvents.any { it.id == eventId }

    val isFinished = event?.let { isEventFinished(it.date, it.timeEnd) } ?: false

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

            if (event != null && event.id == eventId) {
                val isMyEvent = event.creatorId != null && event.creatorId == currentUserId
                
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        key(event.id) {
                            val imageSource = ImageUrlHelper.fixImageUrl(event.thumbnailUri) 
                                ?: event.thumbnailResId 
                                ?: R.drawable.placeholder_poster
                            
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageSource)
                                    .crossfade(false) // Disable crossfade - prevents flash transition
                                    .build(),
                                placeholder = painterResource(R.drawable.placeholder_poster),
                                error = painterResource(R.drawable.placeholder_poster),
                                contentDescription = "Event Banner",
                                contentScale = ContentScale.Fit, // Changed from Crop to Fit - shows full poster
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2f / 3f) // Standard portrait poster ratio (width:height = 2:3)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        EventDetailTable(event = event)

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isRegistered) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (isFinished) {
                                    Button(
                                        onClick = {
                                            navController.navigate(Screen.AllFeedback.createRoute(event.id))
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                    ) {
                                        Text("Feedback")
                                    }
                                }

                                Button(
                                    onClick = {
                                        navController.navigate(Screen.AllDocumentation.createRoute(event.id))
                                    },
                                    modifier = if (isFinished) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                ) {
                                    Text("Dokumentasi")
                                }
                            }
                        } else {
                            if (isMyEvent) {
                                Button(
                                    onClick = {
                                        navController.navigate(Screen.MyRegisteredEvent.createRoute(""))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Kelola Event", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                val isStarted = event.let { EventTimeHelper.isEventStarted(it.date, it.timeStart) }
                                val canRegister = EventTimeHelper.canRegisterForEvent(event.date, event.timeStart)
                                
                                when {
                                    isFinished -> {
                                        Button(
                                            onClick = { /* Disabled */ },
                                            enabled = false,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Gray,
                                                disabledContainerColor = Color.Gray
                                            )
                                        ) {
                                            Text("Event Sudah Selesai", color = Color.White)
                                        }
                                    }
                                    isStarted && !isFinished -> {
                                        Button(
                                            onClick = {  },
                                            enabled = false,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFF9800),
                                                disabledContainerColor = Color(0xFFFF9800)
                                            )
                                        ) {
                                            Text("Pendaftaran Ditutup", color = Color.White)
                                        }
                                    }
                                    else -> {
                                        val quotaInt = event.quota.toIntOrNull() ?: 0
                                        val isQuotaFull = quotaInt > 0 && registrationCount >= quotaInt
                                        
                                        if (isQuotaFull) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color(0xFFFFEBEE) // Light red background
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = "😔",
                                                            fontSize = 28.sp
                                                        )
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text(
                                                                text = "Kuota Sudah Penuh!",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 16.sp,
                                                                color = Color(0xFFD32F2F)
                                                            )
                                                            Text(
                                                                text = "Pendaftaran: $registrationCount/$quotaInt peserta",
                                                                fontSize = 13.sp,
                                                                color = Color(0xFF757575)
                                                            )
                                                        }
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(12.dp))

                                                Button(
                                                    onClick = { },
                                                    enabled = false,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color.Gray,
                                                        disabledContainerColor = Color.Gray
                                                    )
                                                ) {
                                                    Text("Tidak Dapat Mendaftar", color = Color.White)
                                                }
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                if (quotaInt > 0 && !isLoadingCount) {
                                                    val sisaKuota = quotaInt - registrationCount
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = Color(0xFFE8F5E9) // Light green
                                                        )
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.Center
                                                        ) {
                                                            Text(
                                                                text = "✅",
                                                                fontSize = 18.sp
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = "Sisa kuota: $sisaKuota dari $quotaInt",
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = PrimaryGreen
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                }
                                                
                                                PrimaryButton(
                                                    text = "Daftar Sekarang",
                                                    onClick = {
                                                        navController.navigate(Screen.RegistrationFormScreen.createRoute(event.id))
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
    val details = listOf(
        "Judul Event" to event.title,
        "Jenis Event" to event.type,
        "Tanggal" to event.date,
        "Waktu Mulai" to event.timeStart,
        "Waktu Selesai" to event.timeEnd,
        "Tipe Lokasi" to event.platformType,
        "Lokasi/Link" to event.locationDetail,
        "Kuota" to event.quota
    )

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
