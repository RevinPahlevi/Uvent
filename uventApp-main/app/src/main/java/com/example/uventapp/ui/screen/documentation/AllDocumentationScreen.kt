package com.example.uventapp.ui.screen.documentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.R
import com.example.uventapp.data.model.Documentation
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.ui.screen.profile.ProfileViewModel
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
import com.example.uventapp.utils.ImageUrlHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllDocumentationScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    profileViewModel: ProfileViewModel,
    eventId: Int?
) {
    val context = LocalContext.current
    val currentUserProfile by profileViewModel.profile
    val currentUserId = currentUserProfile?.id ?: 0
    val event = remember(eventId, viewModel.allEvents.value, viewModel.createdEvents.value, viewModel.followedEvents) {
        (viewModel.allEvents.value + viewModel.createdEvents.value + viewModel.followedEvents).find { it.id == eventId }
    }

    val canAddDocumentation = remember(event) {
        if (event == null) return@remember false
        
        try {
            val dateFormat = java.text.SimpleDateFormat("d/M/yyyy HH:mm", java.util.Locale.getDefault())
            val now = java.util.Calendar.getInstance()

            val eventStartDateTime = dateFormat.parse("${event.date} ${event.timeStart}")

            val eventEndDateTime = dateFormat.parse("${event.date} ${event.timeEnd}")
            val eventEndPlus3Hours = java.util.Calendar.getInstance().apply {
                time = eventEndDateTime!!
                add(java.util.Calendar.HOUR_OF_DAY, 3)
            }

            val isAfterStart = now.time.after(eventStartDateTime) || now.time == eventStartDateTime
            val isBeforeEndPlus3 = now.time.before(eventEndPlus3Hours.time)
            
            isAfterStart && isBeforeEndPlus3
        } catch (e: Exception) {
            android.util.Log.e("AllDocScreen", "Error parsing event time: ${e.message}")
            false
        }
    }

    val documentationsState by viewModel.documentations
    val documentationList = documentationsState[eventId ?: -1] ?: emptyList()

    var isLoading by remember { mutableStateOf(documentationList.isEmpty()) }

    LaunchedEffect(eventId) {
        if (eventId != null && eventId > 0) {
            try {
                viewModel.loadDocumentationsFromApi(eventId, currentUserId, context)
            } catch (e: Exception) {
                android.util.Log.e("AllDocScreen", "Error loading docs: ${e.message}")
            }
        }
        kotlinx.coroutines.delay(500)
        isLoading = false
    }

    var showDeleteDialog by remember { mutableStateOf<Documentation?>(null) }

    Scaffold(
        topBar = {
            CustomAppBar(title = "Dokumentasi", onBack = { navController.popBackStack() })
        },
        floatingActionButton = {
            if (canAddDocumentation) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.AddDocumentation.createRoute(eventId ?: -1))
                    },
                    containerColor = PrimaryGreen,
                    contentColor = White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah Dokumentasi")
                }
            }
        },
        containerColor = LightBackground
    ) { paddingValues ->

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            documentationList.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada dokumentasi untuk event ini.", color = Color.Gray)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(documentationList, key = { it.id }) { doc ->
                        val isOwn = doc.isAnda || doc.userId == currentUserId
                        DocumentationCard(
                            doc = doc,
                            isOwn = isOwn,
                            onEditClick = {
                                navController.navigate(Screen.AddDocumentation.createEditRoute(doc.eventId, doc.id))
                            },
                            onDeleteClick = {
                                showDeleteDialog = doc
                            }
                        )
                    }
                }
            }
        }

        showDeleteDialog?.let { docToDelete ->
            DeleteConfirmDialog(
                onDismiss = { showDeleteDialog = null },
                onConfirm = {
                    viewModel.deleteDocumentation(docToDelete.eventId, docToDelete.id, currentUserId, context)
                    showDeleteDialog = null
                }
            )
        }
    }
}

@Composable
private fun DocumentationCard(
    doc: Documentation,
    isOwn: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val cardColor = if (isOwn) Color(0xFF00897B) else White
    val textColor = if (isOwn) White else Color.Black
    val alignment = if (isOwn) Arrangement.End else Arrangement.Start
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = if (isOwn) 16.dp else 4.dp,
                topEnd = if (isOwn) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isOwn) White else PrimaryGreen,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isOwn) White.copy(alpha = 0.2f) else PrimaryGreen.copy(alpha = 0.1f))
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isOwn) "Anda" else doc.userName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = textColor
                            )
                            Text(
                                text = doc.postTime,
                                fontSize = 11.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    if (isOwn) {
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opsi",
                                    tint = White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        menuExpanded = false
                                        onEditClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Hapus", color = Color.Red) },
                                    onClick = {
                                        menuExpanded = false
                                        onDeleteClick()
                                    }
                                )
                            }
                        }
                    }
                }

                if (!doc.photoUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(ImageUrlHelper.fixImageUrl(doc.photoUri))
                            .crossfade(true)
                            .error(R.drawable.placeholder_poster)
                            .build(),
                        contentDescription = "Foto Dokumentasi",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                if (doc.description.isNotEmpty()) {
                    Text(
                        text = doc.description,
                        fontSize = 14.sp,
                        color = textColor,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
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
                Text(
                    text = "Hapus Dokumentasi?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Dokumentasi yang dihapus tidak dapat dikembalikan.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Hapus")
                    }
                }
            }
        }
    }
}