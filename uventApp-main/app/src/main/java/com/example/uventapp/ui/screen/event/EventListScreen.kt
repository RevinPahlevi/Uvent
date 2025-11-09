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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.DisabledBackground
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
// Import Event dan dummyEvents dari paket data.model
import com.example.uventapp.data.model.Event
import com.example.uventapp.data.model.dummyEvents


// --- DEFINISI LOKAL DIHAPUS ---
// data class Event(...) dan val dummyEvents DIHAPUS dari sini

@Composable
fun EventListScreen(navController: NavController) {
    val categories = listOf("Semua", "Seminar", "Workshop", "Talkshow", "Skill Lab")

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    // Gunakan 'dummyEvents' yang diimpor
    var filteredEvents by remember { mutableStateOf(dummyEvents) }

    fun filterEvents(category: String) {
        filteredEvents = when (category) {
            "Semua" -> dummyEvents // Gunakan data dummy impor
            else -> dummyEvents.filter { it.type.equals(category, ignoreCase = true) } // Gunakan data dummy impor
        }
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
                    filteredEvents = dummyEvents.filter { event ->
                        event.title.contains(searchText, ignoreCase = true)
                    }.filter { event ->
                        selectedCategory == "Semua" || event.type.equals(selectedCategory, ignoreCase = true)
                    }
                }
                ,
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
                            filterEvents(category)
                        }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEvents) { event ->
                    EventCard(
                        event = event,
                        onClick = { navController.navigate("${Screen.DetailEvent}/${event.id}") }
                    )
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

@Composable
fun EventCard(event: Event, onClick: () -> Unit) { // 'Event' di sini akan merujuk ke model yang diimpor
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
            Image(
                painter = painterResource(id = event.thumbnailResId),
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
                    Text(event.date, fontSize = 12.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Lokasi", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.location, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}
