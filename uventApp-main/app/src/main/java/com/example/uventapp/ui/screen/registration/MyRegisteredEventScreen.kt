package com.example.uventapp.ui.screen.registration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Import Coil
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage // Import Coil
import coil.request.ImageRequest // Import Coil
import com.example.uventapp.R
import com.example.uventapp.data.model.Event // Import dari data.model
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.screen.event.EventManagementViewModel
// Import EventCard dari package-nya
import com.example.uventapp.ui.screen.event.EventCard
import com.example.uventapp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRegisteredEventScreen(
    navController: NavController,
    viewModel: EventManagementViewModel, // Terima ViewModel
    eventName: String = ""
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- LOGIKA DARI MyEventsScreen ---
    val createdEvents by viewModel.createdEvents
    val notificationMessage by viewModel.notificationMessage
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }
    // ---------------------------------

    // --- LOGIKA DARI MyRegisteredEventScreen ---
    val followedEvents by remember(viewModel.followedEvents) {
        derivedStateOf { viewModel.followedEvents }
    }
    var showCancelDialog by remember { mutableStateOf<Event?>(null) } // Ganti ke tipe Event
    var showSuccessBanner by remember { mutableStateOf<String?>(null) }
    // ---------------------------------------

    var selectedTab by remember { mutableStateOf(1) } // Default ke "Diikuti"

    // Tampilkan snackbar untuk event *dibuat*
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

    // Tampilkan snackbar untuk event *didaftar*
    LaunchedEffect(eventName) {
        if (eventName.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Pendaftaran $eventName Berhasil",
                    duration = SnackbarDuration.Short
                )
            }
            // Set tab ke "Diikuti"
            selectedTab = 1
        }
    }

    // Sembunyikan banner "Batal"
    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner != null) {
            delay(3000L)
            showSuccessBanner = null
        }
    }

    val categories = listOf("Semua", "Seminar", "Workshop", "Talkshow", "Skill Lab")
    var selectedCategory by remember { mutableStateOf("Semua") }

    Scaffold(
        topBar = {
            CustomAppBar(title = "Event Saya", onBack = { navController.popBackStack() })
        },
        bottomBar = { BottomNavBar(navController = navController) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        // --- FAB (TOMBOL + BULAT) DITAMBAHKAN ---
        floatingActionButton = {
            // Hanya tampil jika tab "Dibuat" aktif
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddEvent.route) },
                    containerColor = PrimaryGreen,
                    contentColor = White,
                    shape = CircleShape // Sesuai gambar
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah Event")
                }
            }
        },
        containerColor = LightBackground
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tab "Dibuat" dan "Diikuti"
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PrimaryGreen,
                    contentColor = White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            height = 3.dp,
                            color = White
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }, // Hanya ganti tab
                        text = { Text("Dibuat") },
                        selectedContentColor = White,
                        unselectedContentColor = White.copy(alpha = 0.8f)
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }, // Hanya ganti tab
                        text = { Text("Diikuti") },
                        selectedContentColor = White,
                        unselectedContentColor = White.copy(alpha = 0.8f)
                    )
                }

                // Filter Kategori
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                        .padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryButton(
                            text = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- KONTEN BERDASARKAN TAB ---
                when (selectedTab) {
                    // TAB "DIBUAT"
                    0 -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp) // Agar tidak tertutup FAB
                    ) {
                        items(createdEvents, key = { it.id }) { event ->
                            Column {
                                // Card Event (dari model Event)
                                EventCard( // Memanggil EventCard dari EventListScreen.kt
                                    event = event,
                                    onClick = {
                                        navController.navigate(Screen.EditEvent.createRoute(event.id))
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
                                            navController.navigate(Screen.EditEvent.createRoute(event.id))
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF1E88E5), // Biru
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Edit", fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { showDeleteDialog = event.id }, // Tampilkan dialog
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFE53935), // Merah
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Hapus", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // TAB "DIIKUTI"
                    1 -> LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // PERBAIKAN: Gunakan list dari ViewModel
                        if (followedEvents.any { it.status == "Selesai" }) {
                            item {
                                FeedbackBanner()
                            }
                        }
                        // PERBAIKAN: Gunakan list dari ViewModel
                        items(followedEvents, key = { it.id }) { event ->
                            MyEventCard(
                                event = event, // Kirim objek Event
                                navController = navController,
                                onCancelClick = { showCancelDialog = event },
                                onReviewClick = { /* TODO */ }
                            )
                        }
                    }
                }
            } // Akhir Column

            // Banner Sukses Batal
            CancelSuccessBanner(
                eventName = showSuccessBanner,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = paddingValues.calculateTopPadding() + 16.dp)
                    .padding(horizontal = 16.dp)
            )

            // Dialog Konfirmasi Batal
            showCancelDialog?.let { eventToCancel ->
                CancelConfirmationDialog(
                    eventName = eventToCancel.title,
                    onDismiss = { showCancelDialog = null },
                    onConfirm = {
                        showSuccessBanner = eventToCancel.title
                        // TODO: Anda perlu menambahkan fungsi di ViewModel untuk
                        // menghapus event dari _followedEvents
                        // viewModel.unfollowEvent(eventToCancel.id)
                        showCancelDialog = null
                    }
                )
            }

            // Dialog Konfirmasi Hapus
            showDeleteDialog?.let { eventIdToCancel ->
                DeleteConfirmationDialog(
                    onDismiss = { showDeleteDialog = null },
                    onConfirm = {
                        viewModel.deleteEvent(eventIdToCancel)
                        showDeleteDialog = null
                    }
                )
            }
        } // Akhir Box
    }
}

// --- SEMUA COMPOSABLE HELPER DI BAWAH INI ---

@Composable
fun FeedbackBanner() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4E7)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Feedback",
                tint = PrimaryGreen,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, CircleShape)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Waktunya beri feedback!",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    fontSize = 15.sp
                )
                Text(
                    text = "Event \"Business Talkshow\" sudah selesai. Bagikan pengalaman dan foto suasana eventmu!",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { /* TODO: Navigasi ke beri ulasan */ },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Text("Beri Ulasan", fontSize = 11.sp)
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}


@Composable
fun MyEventCard(
    event: Event, // PERBAIKAN: Ganti dari MyEvent ke Event
    navController: NavController,
    onCancelClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigasi ke detail event terdaftar */ }
    ) {
        Box {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    // --- PERUBAHAN UNTUK REQUEST 2 (URI) ---
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(event.thumbnailUri ?: event.thumbnailResId ?: R.drawable.placeholder_poster)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.placeholder_poster),
                        contentDescription = "Event Poster",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    // ----------------------------------------

                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Text(event.type, fontSize = 13.sp, color = PrimaryGreen, modifier = Modifier.padding(bottom = 4.dp))
                        EventInfoRow(icon = Icons.Default.CalendarToday, text = event.date)
                        EventInfoRow(icon = Icons.Default.LocationOn, text = event.location)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // PERBAIKAN: Sesuaikan logika status
                    if (event.status == "Aktif" || event.status == "Terdaftar" || event.status == "Baru") {

                        // --- PERBAIKAN UNTUK REQUEST 1 ---
                        // Ganti IconButton menjadi Button agar ukurannya sama
                        Button(
                            onClick = {
                                navController.navigate(Screen.EditRegistration.createRoute(event.id))
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            // Terapkan modifier & padding yang sama dengan tombol "Batal"
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = White,
                                modifier = Modifier.size(16.dp) // Ukuran ikon tetap
                            )
                        }
                        // ---------------------------------

                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onCancelClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), // Merah
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Batal", fontSize = 12.sp)
                        }
                    } else if (event.status == "Selesai" || event.status == "Berakhir") {
                        Button(
                            onClick = onReviewClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Beri Ulasan", fontSize = 12.sp)
                        }
                    }
                }
            }

            Text(
                text = event.status,
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        color = when (event.status) {
                            "Selesai" -> PrimaryGreen
                            "Berakhir" -> Color(0xFFE53935)
                            "Terdaftar" -> Color(0xFF1E88E5) // Biru
                            "Aktif" -> Color(0xFF1E88E5) // Biru
                            "Baru" -> Color(0xFF1E88E5) // Biru
                            else -> Color.Gray
                        },
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun CategoryButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) PrimaryGreen else Color.LightGray
    val containerColor = if (isSelected) PrimaryGreen else White
    val contentColor = if (isSelected) White else Color.Gray

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun EventInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 13.sp, color = Color.Gray)
    }
}

// --- PERBAIKAN UNTUK REQUEST 2 ---
// Mengganti AlertDialog dengan Dialog Kustom
@Composable
fun CancelConfirmationDialog(
    eventName: String, // eventName tidak dipakai di UI baru, tapi kita biarkan
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Gunakan Dialog standar untuk overlay
    Dialog(onDismissRequest = onDismiss) {
        // Card kustom sesuai gambar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier.width(300.dp) // Beri lebar tetap agar tidak terlalu lebar
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Ikon X di lingkaran hijau
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close, // Ikon 'X'
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // 2. Teks Judul
                Text(
                    text = "Batalkan Pendaftaran?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // 3. Teks Subjudul
                Text(
                    text = "Apakah anda yakin ingin membatalkan pesanan?",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center, // Teks di tengah
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Tombol Ya / Tidak
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tombol Ya (Hijau)
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Ya")
                    }

                    // Tombol Tidak (Merah)
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935), // Warna merah
                            contentColor = Color.White
                        )
                    ) {
                        Text("Tidak")
                    }
                }
            }
        }
    }
}
// ------------------------------------

@Composable
fun CancelSuccessBanner(
    eventName: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = eventName != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(4.dp),
            border = BorderStroke(2.dp, PrimaryGreen)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = eventName ?: "",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Pendaftaran Event Dibatalkan",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

// Dialog Konfirmasi Hapus untuk Event Dibuat
@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
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
                            containerColor = Color(0xFFE0E0E0),
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