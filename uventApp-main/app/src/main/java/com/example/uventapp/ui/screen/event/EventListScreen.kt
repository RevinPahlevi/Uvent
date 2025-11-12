package com.example.uventapp.ui.screen.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext // Import Coil
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage // Import Coil
import coil.request.ImageRequest // Import Coil
import com.example.uventapp.R // Pastikan R di-import
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.* // Import semua theme
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.model.dummyEvents

@Composable
fun EventListScreen(
    navController: NavController,
    viewModel: EventManagementViewModel
) {
    val categories = listOf("Semua", "Seminar", "Workshop", "Talkshow", "Skill Lab")

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }

    val createdEvents by viewModel.createdEvents

    // --- PERBAIKAN 1: Ambil daftar event yang diikuti ---
    // 'followedEvents' adalah State<List<Event>> atau list, kita perlu nilainya
    // Di ViewModel, 'followedEvents' adalah List, jadi kita bisa baca langsung
    val followedEvents = viewModel.followedEvents

    // --- PERBAIKAN 2: Gabungkan event dan filter yang sudah diikuti ---
    // 'remember' akan mengkalkulasi ulang jika createdEvents, dummyEvents, atau followedEvents berubah
    val allAvailableEvents = remember(createdEvents, dummyEvents, followedEvents) {
        // 1. Buat daftar ID event yang sudah diikuti
        val followedEventIds = followedEvents.map { it.id }.toSet()

        // 2. Gabungkan event dummy dan event buatan
        val allEvents = (dummyEvents + createdEvents).distinctBy { it.id }

        // 3. Filter: HANYA tampilkan event yang ID-nya TIDAK ADA di 'followedEventIds'
        allEvents.filter { it.id !in followedEventIds }
    }

    // 'filteredEvents' sekarang akan di-trigger oleh 'allAvailableEvents'
    var filteredEvents by remember { mutableStateOf(allAvailableEvents) } // Inisialisasi awal

    // Fungsi filter yang diperbarui untuk mencari dari 'allAvailableEvents'
    fun applyFilter() {
        filteredEvents = allAvailableEvents.filter { event -> // <-- Gunakan 'allAvailableEvents'
            val categoryMatch = (selectedCategory == "Semua" || event.type.equals(selectedCategory, ignoreCase = true))
            val searchMatch = (searchText.isEmpty() || event.title.contains(searchText, ignoreCase = true))
            categoryMatch && searchMatch
        }
    }

    // Panggil applyFilter saat 'allAvailableEvents' atau filter berubah
    LaunchedEffect(allAvailableEvents, selectedCategory, searchText) { // <-- Ganti 'allEvents' jadi 'allAvailableEvents'
        applyFilter()
    }

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

            if (filteredEvents.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onClick = { navController.navigate(Screen.DetailEvent.createRoute(event.id)) }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Tidak ada event ditemukan", color = Color.Gray)
                }
            }
        }
    }
}

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

// Pastikan fungsi ini TIDAK private agar bisa diakses MyRegisteredEventScreen.kt
@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.thumbnailUri ?: event.thumbnailResId ?: R.drawable.placeholder_poster)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.placeholder_poster),
                contentDescription = "Event Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
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
                    // --- PERBAIKAN DI SINI ---
                    // Ganti dari event.location menjadi event.locationDetail
                    Text(event.locationDetail, fontSize = 12.sp, color = Color.Gray)
                    // -------------------------
                }
            }
        }
    }
}