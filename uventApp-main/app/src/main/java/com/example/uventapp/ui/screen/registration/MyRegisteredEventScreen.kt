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
// Hapus TabRowDefaults
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
import androidx.compose.ui.text.style.TextOverflow // Import
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
// Hapus import EventCard karena kita akan buat versi kustom
import com.example.uventapp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
// --- IMPORT API LAMA (API < 26) ---
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// ------------------------------------------

// --- FUNGSI HELPER MENGGUNAKAN API LAMA ---
private fun isEventFinished(date: String, timeEnd: String): Boolean {
    return try {
        val eventEndString = "$date $timeEnd"
        val formatter = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
        val eventEndDate: Date = formatter.parse(eventEndString) ?: return false
        val now = Date()
        now.after(eventEndDate)
    } catch (e: ParseException) {
        e.printStackTrace()
        false
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
// -------------------------------------------


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRegisteredEventScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    eventName: String = ""
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val createdEvents by viewModel.createdEvents
    val notificationMessage by viewModel.notificationMessage
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }

    val followedEvents by remember(viewModel.followedEvents) {
        derivedStateOf { viewModel.followedEvents }
    }
    var showCancelDialog by remember { mutableStateOf<Event?>(null) }
    var showSuccessBanner by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableStateOf(0) } // Default "Dibuat"

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

    LaunchedEffect(eventName) {
        if (eventName.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Pendaftaran $eventName Berhasil",
                    duration = SnackbarDuration.Short
                )
            }
            selectedTab = 1 // Pindah ke tab "Diikuti"
        }
    }

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
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddEvent.route) },
                    containerColor = PrimaryGreen,
                    contentColor = White,
                    shape = CircleShape
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
                // --- Tombol Tab Kustom "Dibuat" / "Diikuti" ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(color = Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabButton(
                        text = "Dibuat",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        text = "Diikuti",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
                // ---------------------------------------------

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
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(createdEvents, key = { it.id }) { event ->
                            CreatedEventCard(
                                event = event,
                                onEditClick = {
                                    navController.navigate(Screen.EditEvent.createRoute(event.id))
                                },
                                onDeleteClick = {
                                    showDeleteDialog = event.id
                                }
                            )
                        }
                    }

                    // TAB "DIIKUTI"
                    1 -> LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val firstFinishedEvent = followedEvents.find {
                            isEventFinished(it.date, it.timeEnd)
                        }

                        if (firstFinishedEvent != null) {
                            item {
                                FeedbackBanner(
                                    eventName = firstFinishedEvent.title
                                )
                            }
                        }

                        items(followedEvents, key = { it.id }) { event ->
                            val isFinished = isEventFinished(event.date, event.timeEnd)
                            MyEventCard(
                                event = event,
                                isFinished = isFinished,
                                navController = navController,
                                onCancelClick = { showCancelDialog = event },
                                onReviewClick = { /* TODO: Navigasi ke Lihat Ulasan */ }
                            )
                        }
                    }
                }
            } // Akhir Column

            CancelSuccessBanner(
                eventName = showSuccessBanner,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = paddingValues.calculateTopPadding() + 16.dp)
                    .padding(horizontal = 16.dp)
            )

            showCancelDialog?.let { eventToCancel ->
                CancelConfirmationDialog(
                    eventName = eventToCancel.title,
                    onDismiss = { showCancelDialog = null },
                    onConfirm = {
                        showSuccessBanner = eventToCancel.title
                        viewModel.unfollowEvent(eventToCancel.id)
                        showCancelDialog = null
                    }
                )
            }

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

// --- COMPOSABLE HELPER TAB ---
@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) PrimaryGreen else Color.Transparent
    val contentColor = if (isSelected) White else Color.Gray

    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor,
            disabledContentColor = contentColor
        ),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

// --- CARD UNTUK TAB "DIBUAT" ---
@Composable
private fun CreatedEventCard(
    event: Event,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top // Rata atas
        ) {
            // Gambar Poster
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.thumbnailUri ?: event.thumbnailResId ?: R.drawable.placeholder_poster)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.placeholder_poster),
                contentDescription = "Event Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp) // Ukuran poster
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Kolom untuk Teks dan Tombol
            Column(modifier = Modifier.weight(1f)) {
                // Judul
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Tanggal
                EventInfoRow(
                    icon = Icons.Filled.CalendarToday,
                    text = "${event.date} - ${event.timeStart}"
                )

                Spacer(modifier = Modifier.height(16.dp)) // Jarak

                // Tombol Edit dan Hapus
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Jarak antar tombol
                ) {
                    // Tombol Edit (Biru)
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f).height(36.dp), // Beri bobot dan tinggi
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5), // Biru
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Edit", fontSize = 12.sp)
                    }

                    // Tombol Hapus (Merah)
                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier.weight(1f).height(36.dp), // Beri bobot dan tinggi
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935), // Merah
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Hapus", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// --- BANNER FEEDBACK ---
@Composable
fun FeedbackBanner(eventName: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4E7)), // Warna hijau muda
        border = BorderStroke(1.dp, PrimaryGreen), // Border hijau
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon Notifikasi
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Feedback",
                tint = White,
                modifier = Modifier
                    .size(36.dp)
                    .background(PrimaryGreen, CircleShape)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Kolom Teks
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Waktunya beri feedback!",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black, // Warna teks
                    fontSize = 15.sp
                )
                Text(
                    text = "Event \"$eventName\" sudah selesai. Bagikan pengalaman dan foto suasana eventmu!",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color.DarkGray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // Tombol Beri Ulasan
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


// --- CARD UNTUK TAB "DIIKUTI" (PERBAIKAN UTAMA) ---
@Composable
fun MyEventCard(
    event: Event,
    isFinished: Boolean,
    navController: NavController,
    onCancelClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box { // Box diperlukan untuk menampung Status Tag

            // --- PERBAIKAN 1: Layout Utama diubah menjadi Row ---
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Kolom Poster (Lebih besar)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(event.thumbnailUri ?: event.thumbnailResId ?: R.drawable.placeholder_poster)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.placeholder_poster),
                    contentDescription = "Event Poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        // --- PERBAIKAN: Ukuran poster disesuaikan ---
                        .width(80.dp)
                        .height(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Kolom Teks dan Aksi
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Bagian Teks Info
                    Column {
                        Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Text(event.type, fontSize = 13.sp, color = PrimaryGreen, modifier = Modifier.padding(bottom = 4.dp))
                        EventInfoRow(icon = Icons.Default.CalendarToday, text = "${event.date} - ${event.timeStart}")
                    }

                    Spacer(modifier = Modifier.height(8.dp)) // Jarak antara teks dan baris bawah

                    // --- PERBAIKAN 2: Baris Aksi (Lokasi & Ulasan / Tombol) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bagian Lokasi
                        EventInfoRow(
                            icon = Icons.Default.LocationOn,
                            text = event.locationDetail, // <-- Ganti ke locationDetail
                            // Beri weight agar mendorong "Lihat Ulasan" ke kanan
                            modifier = Modifier.weight(1f)
                        )

                        // Tampilkan "Lihat ulasan" HANYA jika sudah selesai
                        if (isFinished) {
                            Text(
                                text = "Lihat ulasan >",
                                color = PrimaryGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable { onReviewClick() }
                                    .padding(start = 8.dp) // Beri jarak
                            )
                        }
                    }
                    // ----------------------------------------------------

                    // Tampilkan tombol Edit/Batal HANYA jika BELUM selesai
                    if (!isFinished) {
                        Spacer(modifier = Modifier.height(12.dp)) // Jarak
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // Tombol Edit
                            Button(
                                onClick = {
                                    navController.navigate(Screen.EditRegistration.createRoute(event.id))
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Tombol Batal
                            Button(
                                onClick = onCancelClick,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), // Merah
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Batal", fontSize = 12.sp)
                            }
                        }
                    }
                    // ---------------------------------------
                }
            } // Akhir Row Utama

            // Status Tag (Selesai / Terdaftar)
            val statusText = if (isFinished) "Selesai" else "Terdaftar"
            // Warna Merah untuk Selesai
            val statusColor = if (isFinished) Color(0xFFE53935) else Color(0xFF1E88E5)

            Text(
                text = statusText,
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        color = statusColor,
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

// --- PERBAIKAN: Tambahkan Modifier agar bisa menerima weight ---
@Composable
fun EventInfoRow(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(bottom = 2.dp) // Terapkan modifier di sini
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.Gray,
            maxLines = 1, // Pastikan tidak wrap ke baris baru
            overflow = TextOverflow.Ellipsis // Tambahkan elipsis jika terlalu panjang
        )
    }
}

@Composable
fun CancelConfirmationDialog(
    eventName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            modifier = Modifier.width(300.dp)
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
                        .background(PrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Batalkan Pendaftaran?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Apakah anda yakin ingin membatalkan pesanan?",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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