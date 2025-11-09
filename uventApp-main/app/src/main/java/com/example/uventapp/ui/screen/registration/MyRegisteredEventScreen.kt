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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.uventapp.R
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data class tiruan untuk UI
data class MyEvent(
    val id: Int,
    val title: String,
    val type: String,
    val date: String,
    val location: String,
    val status: String, // "Terdaftar", "Selesai", "Berakhir", "Dibatalkan"
    val posterResId: Int,
    val review: String? = null
)

// Data tiruan
val dummyMyEvents = listOf(
    MyEvent(1, "Business Talkshow", "Talkshow", "Kamis, 31/12/2025", "Auditorium Unand", "Terdaftar", R.drawable.event_talkshow),
    MyEvent(2, "Workshop Social Media Marketing", "Workshop", "Rabu, 04/05/2025", "Auditorium Unand", "Berakhir", R.drawable.event_seminar, "Sangat bermanfaat!"),
    MyEvent(3, "UI/UX Skill Lab", "Skill Lab", "28 Okt 2025", "Lab Komputer FTI", "Selesai", R.drawable.event_skill_lab)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRegisteredEventScreen(
    navController: NavController,
    eventName: String = ""
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State untuk daftar event, dibuat mutable agar bisa dihapus
    val events = remember { mutableStateListOf(*dummyMyEvents.toTypedArray()) }

    // State untuk mengontrol dialog pembatalan
    var showCancelDialog by remember { mutableStateOf<MyEvent?>(null) }

    // State untuk banner sukses (menyimpan nama event yang dibatalkan)
    var showSuccessBanner by remember { mutableStateOf<String?>(null) }

    // Menampilkan Snackbar pendaftaran (jika ada)
    LaunchedEffect(eventName) {
        if (eventName.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Pendaftaran $eventName Berhasil",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Efek untuk menyembunyikan banner sukses setelah 3 detik
    LaunchedEffect(showSuccessBanner) {
        if (showSuccessBanner != null) {
            delay(3000L)
            showSuccessBanner = null
        }
    }

    val categories = listOf("Semua", "Seminar", "Workshop", "Talkshow", "Skill Lab")
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedTab by remember { mutableStateOf(1) } // 0 = Dibuat, 1 = Diikuti

    Scaffold(
        topBar = {
            CustomAppBar(title = "Event Saya", onBack = { navController.popBackStack() })
        },
        bottomBar = { BottomNavBar(navController = navController) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = LightBackground
    ) { paddingValues ->

        // Box untuk menampung banner di atas segalanya
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
                        onClick = { selectedTab = 0 },
                        text = { Text("Dibuat") },
                        selectedContentColor = White,
                        unselectedContentColor = White.copy(alpha = 0.8f)
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
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

                // Daftar Event
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Banner Feedback (jika ada event "Selesai")
                    if (events.any { it.status == "Selesai" }) {
                        item {
                            FeedbackBanner()
                        }
                    }

                    // Daftar Event
                    items(events, key = { it.id }) { event ->
                        MyEventCard(
                            event = event,
                            navController = navController,
                            onCancelClick = {
                                // Tampilkan dialog konfirmasi
                                showCancelDialog = event
                            },
                            onReviewClick = {
                                // TODO: Navigasi ke halaman beri ulasan
                            }
                        )
                    }
                }
            } // Akhir Column

            // --- BANNER SUKSES BATAL (Muncul di atas) ---
            CancelSuccessBanner(
                eventName = showSuccessBanner, // Menggunakan state
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = paddingValues.calculateTopPadding() + 16.dp) // Posisi di bawah TopBar
                    .padding(horizontal = 16.dp)
            )

            // --- DIALOG KONFIRMASI (Muncul di tengah) ---
            showCancelDialog?.let { eventToCancel ->
                CancelConfirmationDialog(
                    eventName = eventToCancel.title,
                    onDismiss = { showCancelDialog = null }, // Tutup dialog
                    onConfirm = {
                        // --- INI PERBAIKANNYA ---
                        showSuccessBanner = eventToCancel.title // Tampilkan banner
                        // ------------------------
                        events.remove(eventToCancel) // Hapus event dari list
                        showCancelDialog = null // Tutup dialog
                    }
                )
            }
        } // Akhir Box
    }
}

// --- COMPOSABLE BARU UNTUK UI ---

@Composable
fun FeedbackBanner() {
    // Ini adalah kode dari respons sebelumnya, sudah sesuai
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
    event: MyEvent,
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
                    Image(
                        painter = painterResource(id = try { event.posterResId } catch (_: Exception) { R.drawable.placeholder_poster }),
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Text(event.type, fontSize = 13.sp, color = PrimaryGreen, modifier = Modifier.padding(bottom = 4.dp))
                        EventInfoRow(icon = Icons.Default.CalendarToday, text = event.date)
                        EventInfoRow(icon = Icons.Default.LocationOn, text = event.location)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- BAGIAN TOMBOL EDIT DAN BATAL/ULASAN ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (event.status == "Terdaftar") {
                        // TOMBOL EDIT (IKON HIJAU)
                        IconButton(
                            onClick = {
                                // Navigasi ke EditRegistrationScreen
                                navController.navigate(Screen.EditRegistration.createRoute(event.title))
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(PrimaryGreen, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        // TOMBOL BATAL (MERAH)
                        Button(
                            onClick = onCancelClick, // Memanggil fungsi dari parameter
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), // Merah
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Batal", fontSize = 12.sp)
                        }
                    } else if (event.status == "Selesai" || event.status == "Berakhir") {
                        // TOMBOL BERI ULASAN (HIJAU)
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

            // Status Tag (Tetap sama)
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
    // Kode ini dari respons sebelumnya, sudah sesuai
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
    // Kode ini dari respons sebelumnya, sudah sesuai
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

// --- COMPOSABLE BARU ---
@Composable
fun CancelConfirmationDialog(
    eventName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // UI Dialog persis seperti di gambar
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Close, // Ikon X
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier
                        .size(24.dp)
                        .background(PrimaryGreen, CircleShape) // Lingkaran Hijau
                        .padding(4.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Batalkan Pendaftaran?", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        text = {
            Text("Apakah anda yakin ingin membatalkan pesanan?", color = Color.DarkGray, fontSize = 14.sp)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen), // Tombol Ya Hijau
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ya")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), // Tombol Tidak Merah
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tidak")
            }
        }
    )
}

// --- COMPOSABLE BARU ---
@Composable
fun CancelSuccessBanner(
    eventName: String?, // Dibuat nullable
    modifier: Modifier = Modifier
) {
    // AnimatedVisibility agar banner muncul dan hilang dengan mulus
    AnimatedVisibility(
        visible = eventName != null, // Tampil jika eventName tidak null
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        // UI Banner persis seperti di gambar
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(4.dp),
            border = BorderStroke(2.dp, PrimaryGreen) // Garis hijau
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle, // Ikon centang hijau
                    contentDescription = "Success",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = eventName ?: "", // Tampilkan nama event
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