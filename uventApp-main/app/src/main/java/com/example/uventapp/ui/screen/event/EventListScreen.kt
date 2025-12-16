package com.example.uventapp.ui.screen.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.*
import com.example.uventapp.data.model.Event
import com.example.uventapp.utils.ImageUrlHelper
import com.example.uventapp.utils.EventTimeHelper

@Composable
fun EventListScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    profileViewModel: com.example.uventapp.ui.screen.profile.ProfileViewModel
) {
    val categories = listOf("Semua", "Seminar", "Workshop", "Talkshow", "Skill Lab")

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }

    val context = LocalContext.current

    // Get current user ID dari ProfileViewModel (RELIABLE!)
    val currentUserProfile by profileViewModel.profile
    val currentUserId = currentUserProfile?.id ?: -1
    
    android.util.Log.d("EventListScreen", "Current user profile: $currentUserProfile")
    android.util.Log.d("EventListScreen", "Current userId: $currentUserId")

    // Load data saat screen dibuka - dengan logging untuk debug
    LaunchedEffect(key1 = currentUserId) {
        android.util.Log.d("EventListScreen", "LaunchedEffect triggered with userId: $currentUserId")
        viewModel.loadAllEvents(context)
        if (currentUserId != -1) {
            android.util.Log.d("EventListScreen", "Loading createdEvents and followedEvents...")
            viewModel.loadFollowedEvents(currentUserId, context)
            viewModel.loadCreatedEvents(currentUserId, context)
        } else {
            android.util.Log.w("EventListScreen", "userId is -1, skipping createdEvents/followedEvents load")
        }
    }

    val allEvents by viewModel.allEvents
    val createdEvents by viewModel.createdEvents
    val followedEvents = viewModel.followedEvents
    val isLoading by viewModel.isLoadingAllEvents
    
    // Debug: Log changes in createdEvents
    LaunchedEffect(createdEvents.size) {
        android.util.Log.d("EventListScreen", "createdEvents size changed: ${createdEvents.size}")
        createdEvents.forEach { 
            android.util.Log.d("EventListScreen", "  - Created event: ${it.id} - ${it.title}")
        }
    }
    LaunchedEffect(followedEvents.size) {
        android.util.Log.d("EventListScreen", "followedEvents size changed: ${followedEvents.size}")
        followedEvents.forEach {
            android.util.Log.d("EventListScreen", "  - Followed event: ${it.id} - ${it.title}")
        }
    }
    
    // CRITICAL: Pre-compute badge status OUTSIDE items loop untuk reliable recomposition
    val eventBadgeStatus = remember(createdEvents, followedEvents) {
        val createdIds = createdEvents.map { it.id }.toSet()
        val followedIds = followedEvents.map { it.id }.toSet()
        android.util.Log.d("EventListScreen", "Computing badge status: created=${createdIds.size}, followed=${followedIds.size}")
        
        mapOf(
            "created" to createdIds,
            "followed" to followedIds
        )
    }

    // PERBAIKAN BESAR: TIDAK ADA FILTER! Semua event ditampilkan.
    // Hanya filter berdasarkan kategori dan search, TIDAK filter by user
    val filteredEvents by remember(allEvents, selectedCategory, searchText) {
        derivedStateOf {
            allEvents.filter { event ->
                // Filter HANYA berdasarkan kategori dan search text
                val categoryMatch = (selectedCategory == "Semua" || event.type.equals(selectedCategory, ignoreCase = true))
                val searchMatch = (searchText.isEmpty() || event.title.contains(searchText, ignoreCase = true))
                // Filter event yang sudah selesai
                val notFinished = !EventTimeHelper.isEventFinished(event.date, event.timeEnd)
                categoryMatch && searchMatch && notFinished
            }
        }
    }
    // --- HAPUS BLOK KODE LAMA DI BAWAH INI ---
    // var filteredEvents by remember { mutableStateOf(allAvailableEvents) }
    // fun applyFilter() { ... }
    // LaunchedEffect(allAvailableEvents, selectedCategory, searchText) { ... }
    // ------------------------------------

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Event Tersedia",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                placeholder = { Text("Cari Event..", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DisabledBackground,
                    unfocusedContainerColor = DisabledBackground,
                    disabledContainerColor = DisabledBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = PrimaryGreen
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryButton(
                        text = category,
                        isSelected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                        }
                    )
                }
            }

            // ✅ PERBAIKAN: 3-state logic dengan loading indicator
            when {
                isLoading -> {
                    // State 1: Loading - show CircularProgressIndicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                filteredEvents.isNotEmpty() -> {
                    // State 2: Has data - show event list
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredEvents, key = { it.id }) { event ->
                            // Get badge status from pre-computed map
                            val isMyEvent = eventBadgeStatus["created"]?.contains(event.id) == true
                            val isRegistered = eventBadgeStatus["followed"]?.contains(event.id) == true
                            
                            android.util.Log.d("EventListScreen", "Event ${event.id}: isMyEvent=$isMyEvent, isRegistered=$isRegistered")
                            
                            EventCard(
                                event = event,
                                isMyEvent = isMyEvent,
                                isRegistered = isRegistered,
                                onClick = { navController.navigate(Screen.DetailEvent.createRoute(event.id)) }
                            )
                        }
                    }
                }
                else -> {
                    // State 3: Empty - show empty message (ONLY when NOT loading)
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada event ditemukan", color = Color.Gray)
                    }
                }
            }
        }
    }
}

// (CategoryButton dan EventCard tidak berubah)
@Composable
fun CategoryButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryGreen else White,
            contentColor = if (isSelected) White else Color.Gray
        ),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun EventCard(
    event: Event,
    isMyEvent: Boolean = false,
    isRegistered: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fix image URL for Android
                val imageSource = ImageUrlHelper.fixImageUrl(event.thumbnailUri)
                    ?: event.thumbnailResId
                    ?: R.drawable.placeholder_poster
                
                // PERBAIKAN: Disable cache untuk fix flash gambar lama
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageSource)
                        .diskCachePolicy(coil.request.CachePolicy.DISABLED)
                        .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                        .build(),
                    error = painterResource(R.drawable.placeholder_poster),
                    contentDescription = "Event Poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text(event.type, fontSize = 14.sp, color = PrimaryGreen, modifier = Modifier.padding(bottom = 4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = "Tanggal", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${event.date} - ${event.timeStart}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "Lokasi", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(event.locationDetail, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            
            // Badge status di POJOK KANAN ATAS (EXACT sama dengan MyRegisteredEventScreen)
            when {
                isMyEvent -> {
                    // Badge hijau untuk event yang user buat
                    Text(
                        text = "Event Anda",
                        color = White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(
                                color = PrimaryGreen,
                                shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                isRegistered -> {
                    // Badge biru untuk event yang sudah didaftar
                    Text(
                        text = "Sudah Terdaftar",
                        color = White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(
                                color = Color(0xFF2196F3),
                                shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
