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
import com.example.uventapp.R
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.*

data class Event(
    val id: Int,
    val title: String,
    val type: String,
    val date: String,
    val time: String,
    val location: String,
    val quota: String,
    val status: String,
    val thumbnailResId: Int
)

val dummyEvents = listOf(
    Event(1, "Business Talkshow", "Talkshow", "16 Okt 2025", "", "Auditorium Unand", "", "", R.drawable.event_talkshow),
    Event(2, "Seminar Nasional AI", "Seminar", "20 Okt 2025", "", "Convention Hall Unand", "", "", R.drawable.event_seminar),
    Event(3, "UI/UX Skill Lab", "Skill Lab", "28 Okt 2025", "", "Lab Komputer FTI", "", "", R.drawable.event_skill_lab)
)

@Composable
fun EventListScreen(navController: NavController) {
    val categories = listOf("Semua", "Seminar", "Workshop", "Talkshow", "Skill Lab")
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var filteredEvents by remember { mutableStateOf(dummyEvents) }

    fun applyFilter() {
        filteredEvents = dummyEvents.filter {
            (selectedCategory == "Semua" || it.type.equals(selectedCategory, ignoreCase = true)) &&
                    it.title.contains(searchText, ignoreCase = true)
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
                    applyFilter()
                },
                placeholder = { Text("Cari Event..", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.Gray) },
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
                        isSelected = selectedCategory == category
                    ) {
                        selectedCategory = category
                        applyFilter()
                    }
                }
            }

            if (filteredEvents.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEvents) { event ->
                        EventCard(event) {
                            navController.navigate("${Screen.DetailEvent.route}/${event.id}")
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
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
            val painter = runCatching { painterResource(id = event.thumbnailResId) }.getOrNull()
            if (painter != null) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(event.type, fontSize = 14.sp, color = PrimaryGreen, modifier = Modifier.padding(bottom = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.date, fontSize = 12.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.location, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}
