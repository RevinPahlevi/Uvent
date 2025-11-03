package com.example.uventapp.ui.screen.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class RegisteredEvent(
    val id: Int,
    val title: String,
    val date: String,
    val location: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRegisteredEventScreen(
    eventName: String = "",
    registeredEvents: List<RegisteredEvent> = listOf(
        RegisteredEvent(1, "Seminar AI dan Data Science", "12 November 2025", "Aula Teknik UNAND", "Terdaftar"),
        RegisteredEvent(2, "Workshop UI/UX Design", "15 November 2025", "Gedung Serbaguna PNP", "Selesai")
    ),
    onEditClick: (RegisteredEvent) -> Unit = {},
    onCancelClick: (RegisteredEvent) -> Unit = {},
    onReviewClick: (RegisteredEvent) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(eventName) {
        if (eventName.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar("$eventName Pendaftaran Event Berhasil", duration = SnackbarDuration.Short)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Event yang Saya Daftar") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (registeredEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada event yang kamu daftarkan.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(registeredEvents) { event ->
                    EventItem(
                        event = event,
                        onEditClick = { onEditClick(event) },
                        onCancelClick = { onCancelClick(event) },
                        onReviewClick = { onReviewClick(event) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventItem(
    event: RegisteredEvent,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(event.title, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text("Tanggal: ${event.date}", fontSize = 14.sp)
            Text("Lokasi: ${event.location}", fontSize = 14.sp)
            Text(
                "Status: ${event.status}",
                fontSize = 14.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color = when (event.status) {
                    "Selesai" -> Color(0xFF4CAF50)
                    "Terdaftar" -> Color(0xFF2196F3)
                    else -> Color.Black
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (event.status == "Terdaftar") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onEditClick, modifier = Modifier.weight(1f)) { Text("Edit") }
                    OutlinedButton(onClick = onCancelClick, modifier = Modifier.weight(1f)) { Text("Batal") }
                }
            } else if (event.status == "Selesai") {
                Button(onClick = onReviewClick, modifier = Modifier.fillMaxWidth()) { Text("Beri Ulasan") }
            }
        }
    }
}
