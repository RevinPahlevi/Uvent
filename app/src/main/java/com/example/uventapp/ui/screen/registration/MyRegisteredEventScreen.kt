package com.example.uventapp.ui.screen.registration

import coil.request.CachePolicy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.R
import com.example.uventapp.data.model.Event
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.ui.screen.profile.ProfileViewModel
import com.example.uventapp.ui.theme.*
import com.example.uventapp.utils.ImageUrlHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- HELPER FUNCTION ---
private fun isEventFinished(date: String, timeEnd: String): Boolean {
    try {
        // Parse tanggal (format: "d/M/yyyy" atau "dd/MM/yyyy")
        val dateParts = date.split("/")
        if (dateParts.size != 3) return false
        
        val day = dateParts[0].toIntOrNull() ?: return false
        val month = dateParts[1].toIntOrNull() ?: return false
        val year = dateParts[2].toIntOrNull() ?: return false
        
        // Parse waktu selesai (format: "HH:mm" atau "HH:mm:ss")
        val timeParts = timeEnd.split(":")
        if (timeParts.size < 2) return false
        
        val hour = timeParts[0].toIntOrNull() ?: return false
        val minute = timeParts[1].toIntOrNull() ?: return false
        
        // Buat Calendar untuk waktu akhir event
        val eventEndCalendar = java.util.Calendar.getInstance()
        eventEndCalendar.set(java.util.Calendar.YEAR, year)
        eventEndCalendar.set(java.util.Calendar.MONTH, month - 1) // Calendar month is 0-indexed
        eventEndCalendar.set(java.util.Calendar.DAY_OF_MONTH, day)
        eventEndCalendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
        eventEndCalendar.set(java.util.Calendar.MINUTE, minute)
        eventEndCalendar.set(java.util.Calendar.SECOND, 0)
        eventEndCalendar.set(java.util.Calendar.MILLISECOND, 0)
        
        // Waktu sekarang
        val now = java.util.Calendar.getInstance()
        
        // Event selesai jika waktu sekarang SETELAH waktu akhir event
        return now.after(eventEndCalendar)
    } catch (e: Exception) {
        // Jika ada error parsing, anggap event BELUM selesai (safe default)
        android.util.Log.e("isEventFinished", "Error parsing date=$date, timeEnd=$timeEnd: ${e.message}")
        return false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRegisteredEventScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    profileViewModel: ProfileViewModel,
    eventName: String = ""
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 1. Ambil Profil User
    val currentUserProfile by profileViewModel.profile
    val currentUserId = currentUserProfile?.id

    // 2. State Data Event
    // PERBAIKAN: Gunakan langsung state dari ViewModel, jangan difilter manual dari allEvents
    val createdEvents by viewModel.createdEvents
    val followedEvents = viewModel.followedEvents // List event yang diikuti
    val isLoadingCreatedEvents by viewModel.isLoadingCreatedEvents // Loading state

    // 3. Load Data saat Screen Dibuka
    // Ini PENTING agar event yang baru dibuat/diikuti langsung muncul
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            viewModel.loadCreatedEvents(currentUserId, context)
            viewModel.loadFollowedEvents(currentUserId, context) // Load event yang diikuti
        }
    }

    val notificationMessage by viewModel.notificationMessage
    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }
    var showCancelDialog by remember { mutableStateOf<Event?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    // Efek Notifikasi Snackbar
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
    // Handle navigation from registration success and edit success
    LaunchedEffect(eventName) {
        if (eventName.isNotEmpty()) {
            if (eventName == "_return_to_followed") {
                // Just switch to Diikuti tab without message
                selectedTab = 1
            } else if (eventName == "_no_change") {
                // No changes made - just switch to Diikuti tab without message
                selectedTab = 1
            } else if (eventName == "_edit_success") {
                // Show edit success message and switch to Diikuti tab
                selectedTab = 1
                snackbarHostState.showSnackbar(
                    message = "Perubahan berhasil disimpan",
                    duration = SnackbarDuration.Short
                )
            } else {
                // Registration success - show message and switch to Diikuti tab
                selectedTab = 1
                snackbarHostState.showSnackbar(
                    message = "Berhasil mendaftar event \"$eventName\"!",
                    duration = SnackbarDuration.Short
                )
            }
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
                // --- TAB BUTTONS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(color = Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabButton(text = "Dibuat", isSelected = selectedTab == 0, onClick = { selectedTab = 0 }, modifier = Modifier.weight(1f))
                    TabButton(text = "Diikuti", isSelected = selectedTab == 1, onClick = { selectedTab = 1 }, modifier = Modifier.weight(1f))
                }

                // --- KATEGORI FILTER ---
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryButton(text = category, isSelected = selectedCategory == category, onClick = { selectedCategory = category })
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- LIST EVENT ---
                when (selectedTab) {
                    // TAB "DIBUAT"
                    0 -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Filter list berdasarkan kategori yang dipilih
                        val filteredCreatedEvents = if (selectedCategory == "Semua") {
                            createdEvents
                        } else {
                            createdEvents.filter { it.type.equals(selectedCategory, ignoreCase = true) }
                        }

                        // TAMPILKAN LOADING SKELETON saat data sedang dimuat
                        if (isLoadingCreatedEvents) {
                            items(3) { // Show 3 skeleton items
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE0E0E0))
                                )
                            }
                        } else if (filteredCreatedEvents.isNotEmpty()) {
                            items(
                                items = filteredCreatedEvents,
                                key = { event -> "${event.id}_${event.thumbnailUri}" }
                            ) { event ->
                                val isFinished = isEventFinished(event.date, event.timeEnd)
                                CreatedEventCard(
                                    event = event,
                                    isFinished = isFinished,
                                    navController = navController,
                                    onEditClick = {
                                        navController.navigate(Screen.EditEvent.createRoute(event.id))
                                    },
                                    onDeleteClick = {
                                        showDeleteDialog = event.id
                                    },
                                    onLihatFeedbackClick = {
                                        navController.navigate(Screen.AllFeedback.createRoute(event.id))
                                    }
                                )
                            }
                        } else {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                                    Text("Belum ada event dibuat.", color = Color.Gray)
                                }
                            }
                        }
                    }

                    // TAB "DIIKUTI"
                    1 -> LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val filteredFollowedEvents = if (selectedCategory == "Semua") {
                            followedEvents
                        } else {
                            followedEvents.filter { it.type.equals(selectedCategory, ignoreCase = true) }
                        }

                        val finishedEventNoReview = filteredFollowedEvents.find {
                            isEventFinished(it.date, it.timeEnd) &&
                                    viewModel.getFeedbacksForEvent(it.id).none { f -> f.isAnda }
                        }

                        if (finishedEventNoReview != null) {
                            item {
                                FeedbackBanner(
                                    eventName = finishedEventNoReview.title,
                                    onBannerClick = {
                                        navController.navigate(Screen.AddFeedback.createRoute(finishedEventNoReview.id))
                                    }
                                )
                            }
                        }

                        if (filteredFollowedEvents.isNotEmpty()) {
                            items(
                                items = filteredFollowedEvents,
                                key = { event -> "${event.id}_${event.thumbnailUri}" }
                            ) { event ->
                                val isFinished = isEventFinished(event.date, event.timeEnd)
                                MyEventCard(
                                    event = event,
                                    isFinished = isFinished,
                                    navController = navController,
                                    onCancelClick = { showCancelDialog = event },
                                    onReviewClick = {
                                        if (isFinished) {
                                            navController.navigate(Screen.AllFeedback.createRoute(event.id))
                                        }
                                    }
                                )
                            }
                        } else {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                                    Text("Belum ada event diikuti.", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            // --- DIALOGS ---
            showCancelDialog?.let { eventToCancel ->
                CancelConfirmationDialog(
                    eventName = eventToCancel.title,
                    onDismiss = { showCancelDialog = null },
                    onConfirm = {
                        viewModel.unfollowEvent(
                            eventId = eventToCancel.id,
                            context = context,
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Pendaftaran berhasil dibatalkan")
                                }
                            },
                            onError = { errorMsg ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(errorMsg)
                                }
                            }
                        )
                        showCancelDialog = null
                    }
                )
            }
            showDeleteDialog?.let { eventIdToCancel ->
                DeleteConfirmationDialog(
                    onDismiss = { showDeleteDialog = null },
                    onConfirm = {
                        viewModel.deleteEvent(eventIdToCancel, context)
                        showDeleteDialog = null
                        // Refresh data setelah delete
                        if (currentUserId != null) {
                            viewModel.loadCreatedEvents(currentUserId, context)
                        }
                    }
                )
            }
        }
    }
}

// --- COMPOSABLE COMPONENTS (Tidak Berubah) ---
@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
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

@Composable
private fun CreatedEventCard(
    event: Event,
    isFinished: Boolean,
    navController: NavController,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLihatFeedbackClick: () -> Unit
) {
    // Tentukan warna dan teks status verifikasi
    val (verificationStatusText, verificationStatusColor) = when (event.status.lowercase()) {
        "menunggu" -> "Diproses" to Color(0xFFFF9800) // Orange
        "disetujui" -> "Disetujui" to Color(0xFF4CAF50) // Green
        "ditolak" -> "Ditolak" to Color(0xFFE53935) // Red
        else -> "Diproses" to Color(0xFFFF9800)
    }
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Fix image URL for Android
                val imageSource = ImageUrlHelper.fixImageUrl(event.thumbnailUri)
                    ?: event.thumbnailResId
                    ?: R.drawable.placeholder_poster
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageSource)
                        .crossfade(false) // Disable crossfade to prevent flash
                        .memoryCachePolicy(CachePolicy.DISABLED) // Disable memory cache
                        .diskCachePolicy(CachePolicy.DISABLED)  // Disable disk cache
                        .build(),
                    error = painterResource(R.drawable.placeholder_poster),
                    contentDescription = "Event Poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(80.dp)
                        .height(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text(event.type, fontSize = 13.sp, color = PrimaryGreen)
                    EventInfoRow(icon = Icons.Filled.CalendarToday, text = "${event.date} - ${event.timeStart}")
                    EventInfoRow(icon = Icons.Filled.LocationOn, text = event.locationDetail)
                    
                    // Tombol aksi di bawah lokasi
                    Spacer(modifier = Modifier.height(4.dp))
                    when {
                        isFinished && event.status.lowercase() == "disetujui" -> {
                            // Event selesai dan disetujui - tampilkan tombol Lihat Feedback
                            Button(
                                onClick = onLihatFeedbackClick,
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Text("Lihat Feedback", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                        event.status.lowercase() == "disetujui" && !isFinished -> {
                            // Event disetujui tapi belum selesai - tampilkan Edit, Hapus, & Lihat Peserta
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        navController.navigate(Screen.ParticipantList.createRoute(event.id, event.title))
                                    },
                                    modifier = Modifier.height(32.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) { Text("Peserta", fontSize = 11.sp) }
                                Button(
                                    onClick = onEditClick,
                                    modifier = Modifier.height(32.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5), contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) { Text("Edit", fontSize = 11.sp) }
                                Button(
                                    onClick = onDeleteClick,
                                    modifier = Modifier.height(32.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) { Text("Hapus", fontSize = 11.sp) }
                            }
                        }
                        event.status.lowercase() == "menunggu" -> {
                            // Event masih menunggu verifikasi - hanya tombol Hapus
                            Button(
                                onClick = onDeleteClick,
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) { Text("Batalkan", fontSize = 11.sp) }
                        }
                        event.status.lowercase() == "ditolak" -> {
                            // Event ditolak - hanya tombol Hapus
                            Button(
                                onClick = onDeleteClick,
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) { Text("Hapus", fontSize = 11.sp) }
                        }
                    }
                }
            }
            
            // Status Badge di pojok kanan atas
            Text(
                text = verificationStatusText,
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        color = verificationStatusColor,
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun FeedbackBanner(eventName: String, onBannerClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4E7)),
        border = BorderStroke(1.dp, PrimaryGreen),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Feedback", tint = White, modifier = Modifier.size(36.dp).background(PrimaryGreen, CircleShape).padding(8.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Waktunya beri feedback!", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
                Text(text = "Event \"$eventName\" sudah selesai.", fontSize = 12.sp, color = Color.DarkGray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onBannerClick() }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen), contentPadding = PaddingValues(horizontal = 10.dp)) {
                Text("Beri Ulasan", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun MyEventCard(event: Event, isFinished: Boolean, navController: NavController, onCancelClick: () -> Unit, onReviewClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.DetailEvent.createRoute(event.id)) }
    ) {
        Box {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                // Fix image URL for Android
                val imageSource = ImageUrlHelper.fixImageUrl(event.thumbnailUri)
                    ?: event.thumbnailResId
                    ?: R.drawable.placeholder_poster
                
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageSource)
                        .crossfade(false) // Disable crossfade
                        .memoryCachePolicy(CachePolicy.DISABLED) // Disable memory cache
                        .diskCachePolicy(CachePolicy.DISABLED)   // Disable disk cache
                        .build(),
                    error = painterResource(R.drawable.placeholder_poster),
                    contentDescription = "Event Poster",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.width(80.dp).height(90.dp).clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text(event.type, fontSize = 13.sp, color = PrimaryGreen)
                    EventInfoRow(icon = Icons.Default.CalendarToday, text = "${event.date} - ${event.timeStart}")
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        EventInfoRow(icon = Icons.Default.LocationOn, text = event.locationDetail, modifier = Modifier.weight(1f, fill = false))
                        if (isFinished) {
                            Text(text = "Lihat ulasan >", color = PrimaryGreen, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.clickable { onReviewClick() }.padding(start = 8.dp))
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(onClick = { navController.navigate(Screen.EditRegistration.createRoute(event.id)) }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen), modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = White, modifier = Modifier.size(16.dp))
                                }
                                Button(onClick = onCancelClick, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), contentPadding = PaddingValues(horizontal = 10.dp), modifier = Modifier.height(32.dp)) {
                                    Text("Batal", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
            val statusText = if (isFinished) "Selesai" else "Terdaftar"
            val statusColor = if (isFinished) Color(0xFFE53935) else Color(0xFF1E88E5)
            Text(text = statusText, color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopEnd).background(color = statusColor, shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp)).padding(horizontal = 10.dp, vertical = 4.dp))
        }
    }
}

@Composable
fun CategoryButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryGreen else White,
            contentColor = if (isSelected) White else Color.Gray
        ),
        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun EventInfoRow(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(bottom = 2.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 13.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun CancelConfirmationDialog(eventName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White), modifier = Modifier.width(300.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(PrimaryGreen), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = White, modifier = Modifier.size(32.dp))
                }
                Text(text = "Batalkan Pendaftaran?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = "Apakah anda yakin ingin membatalkan pesanan?", fontSize = 14.sp, textAlign = TextAlign.Center, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White)) { Text("Ya") }
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White)) { Text("Tidak") }
                }
            }
        }
    }
}


@Composable
private fun DeleteConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFFFFEBEE)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(32.dp))
                }
                Text(text = "Hapus Event?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = "Apakah kamu yakin ingin menghapus event ini?", fontSize = 14.sp, textAlign = TextAlign.Center, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black)) { Text("Batal") }
                    Button(onClick = onConfirm, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)) { Text("Ya, hapus") }
                }
            }
        }
    }
}
