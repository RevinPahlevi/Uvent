package com.example.uventapp.ui.screen.documentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Pastikan import ini ada
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert // <-- IMPORT BARU: Titik tiga
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder // --- IMPORT Hati outline ---
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
// --- PERBAIKAN: Import model Documentation ---
import com.example.uventapp.data.model.Documentation
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllDocumentationScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    eventId: Int?
) {
    val context = LocalContext.current
    
    // Ambil data dari getDocumentationForEvent
    val documentationList by remember(viewModel.documentations.value) {
        derivedStateOf { viewModel.getDocumentationForEvent(eventId ?: -1) }
    }

    val likedDocIds by viewModel.likedDocIds

    // --- STATE BARU UNTUK DIALOG HAPUS ---
    var showDeleteDialog by remember { mutableStateOf<Documentation?>(null) }
    // -----------------------------------

    Scaffold(
        topBar = {
            CustomAppBar(title = "Dokumentasi", onBack = { navController.popBackStack() })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigasi ke AddDocumentation untuk 'Tambah Baru'
                    navController.navigate(Screen.AddDocumentation.createRoute(eventId ?: -1))
                },
                containerColor = PrimaryGreen,
                contentColor = White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Dokumentasi")
            }
        },
        containerColor = LightBackground
    ) { paddingValues ->

        if (documentationList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada dokumentasi untuk event ini.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(documentationList, key = { it.id }) { doc ->
                    DocumentationCard(
                        doc = doc,
                        isLiked = likedDocIds.contains(doc.id),
                        onLikeClick = { viewModel.toggleDocumentationLike(doc.id) },
                        onEditClick = {
                            // --- PERBAIKAN DI SINI ---
                            // Navigasi ke rute Edit dengan eventId dan docId
                            navController.navigate(Screen.AddDocumentation.createEditRoute(doc.eventId, doc.id))
                            // -------------------------
                        },
                        onDeleteClick = {
                            showDeleteDialog = doc // Tampilkan dialog
                        }
                    )
                }
            }
        }

        // --- DIALOG KONFIRMASI HAPUS ---
        showDeleteDialog?.let { docToDelete ->
            DeleteDocumentationDialog(
                onDismiss = { showDeleteDialog = null },
                onConfirm = {
                    viewModel.deleteDocumentation(docToDelete.eventId, docToDelete.id, 0, context)
                    showDeleteDialog = null
                }
            )
        }
        // ---------------------------------
    }
}

@Composable
private fun DocumentationCard(
    doc: Documentation,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // State untuk menu dropdown
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // --- Header (Nama, Tanggal, Menu) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = White,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen)
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Kolom untuk Nama, Tanggal, dan Waktu
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${doc.userName}${if (doc.isAnda) " (Anda)" else ""}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (doc.isAnda) PrimaryGreen else Color.Black
                    )
                    // --- PERBAIKAN: Tampilkan tanggal DAN waktu ---
                    Text(
                        text = "${doc.postDate}, ${doc.postTime}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    // --------------------------------------------
                }

                // --- Tombol Titik Tiga (Hanya jika 'isAnda') ---
                if (doc.isAnda) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opsi",
                                tint = Color.Gray
                            )
                        }
                        // --- Menu Dropdown ---
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
                                text = { Text("Hapus") },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
                // ----------------------------------------------------
            }

            // --- Gambar Dokumentasi ---
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(doc.photoUri ?: R.drawable.placeholder_poster)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.placeholder_poster),
                contentDescription = "Foto Dokumentasi",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.LightGray)
            )

            // --- Deskripsi dan Tombol Like ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = doc.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.Gray,
                    modifier = Modifier.clickable {
                        onLikeClick() // Panggil fungsi dari ViewModel
                    }
                )
            }
        }
    }
}

// --- DIALOG HAPUS (BARU) ---
@Composable
private fun DeleteDocumentationDialog(
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
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Apakah kamu yakin ingin menghapus postingan ini?",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
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
                            containerColor = Color(0xFFE53935), // Merah
                            contentColor = Color.White
                        )
                    ) {
                        Text("Ya, Hapus")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Batal")
                    }
                }
            }
        }
    }
}