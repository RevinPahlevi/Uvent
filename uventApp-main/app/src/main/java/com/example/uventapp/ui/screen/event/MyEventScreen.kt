package com.example.uventapp.ui.screen.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.R
import kotlinx.coroutines.launch
import androidx.compose.foundation.background // <-- PERBAIKAN: Import ini ditambahkan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEventsScreen(navController: NavController, viewModel: EventManagementViewModel) {
    val events by viewModel.createdEvents
    val notificationMessage by viewModel.notificationMessage
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Dibuat, 1: Diikuti

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Menampilkan Snackbar ketika ada notifikasi baru dari ViewModel
    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = notificationMessage!!,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearNotification()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Saya", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            // Hanya tampilkan FAB jika di tab "Dibuat"
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddEvent) },
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah Event")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Tab Row (Dibuat / Diikuti)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PrimaryGreen
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Dibuat") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Diikuti") }
                )
            }

            // Konten berdasarkan Tab
            when (selectedTab) {
                // --- TAB DIBUAT ---
                0 -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(events) { event ->
                        Column {
                            // Card Event (menggunakan Composable dari EventListScreen.kt)
                            EventCard(
                                event = event,
                                onClick = {
                                    // Klik pada card di halaman ini mungkin mengarah ke Edit
                                    navController.navigate("${Screen.EditEvent}/${event.id}")
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Tombol Edit dan Hapus (sesuai UI)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        navController.navigate("${Screen.EditEvent}/${event.id}")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE0E0E0),
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Edit", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { showDeleteDialog = event.id }, // Tampilkan dialog
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFCDD2), // Merah muda
                                        contentColor = Color.Red
                                    )
                                ) {
                                    Text("Hapus", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                // --- TAB DIIKUTI ---
                1 -> Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Event yang Anda ikuti akan tampil di sini.", color = Color.Gray)
                }
            }
        }
    }

    // Menampilkan Dialog Konfirmasi Hapus
    if (showDeleteDialog != null) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteEvent(showDeleteDialog!!)
                showDeleteDialog = null
            }
        )
    }
}

/**
 * Dialog konfirmasi Hapus (sesuai image_d8e704.png)
 */
@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ikon Tong Sampah
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)) // <-- 'background' SEKARANG BERFUNGSI
                        .background(Color(0xFFFFEBEE)), // Background merah muda
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Hapus Event?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Apakah kamu yakin ingin menghapus event ini?",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0), // Abu-abu
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Ya, hapus")
                    }
                }
            }
        }
    }
}

