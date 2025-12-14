package com.example.uventapp.ui.screen.registration

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.uventapp.data.model.fakultasList
// --- PERBAIKAN: IMPORT Registration, BUKAN RegistrationData ---
import com.example.uventapp.data.model.Registration
// Removed dummy events import
import com.example.uventapp.ui.screen.event.EventManagementViewModel
// -----------------------------
import com.example.uventapp.ui.components.*
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import kotlinx.coroutines.delay

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRegistrationScreen(
    navController: NavController,
    viewModel: EventManagementViewModel, // Terima ViewModel
    eventId: Int? // Terima eventId
) {
    // --- PENGAMBILAN DATA ---
    val event = remember(eventId, viewModel.allEvents.value, viewModel.createdEvents.value, viewModel.followedEvents) {
        // Cari event di allEvents, createdEvents, atau followedEvents
        (viewModel.allEvents.value + viewModel.createdEvents.value + viewModel.followedEvents).find { it.id == eventId }
    }
    val existingData = remember(eventId) {
        viewModel.getRegistrationData(eventId ?: -1) // <-- Perbaikan tipe
    }
    // ------------------------

    // --- ISI STATE DARI VIEWMODEL, BUKAN DUMMY ---
    var name by remember { mutableStateOf(existingData?.name ?: "") }
    var nim by remember { mutableStateOf(existingData?.nim ?: "") }
    var selectedFakultas by remember { mutableStateOf(existingData?.fakultas ?: "Pilih Fakultas") }
    var selectedJurusan by remember { mutableStateOf(existingData?.jurusan ?: "Pilih Jurusan") }

    // Logika untuk mengisi daftar jurusan berdasarkan fakultas yang ada
    var availableJurusan by remember(selectedFakultas) { // <- Tambah key 'selectedFakultas'
        mutableStateOf(fakultasList.find { it.nama == selectedFakultas }?.jurusan ?: emptyList())
    }

    var email by remember { mutableStateOf(existingData?.email ?: "") }
    var phone by remember { mutableStateOf(existingData?.phone ?: "") }

    // Logika untuk file KRS
    var selectedFileUri by remember { mutableStateOf(existingData?.krsUri?.toUri()) }
    var selectedFileName by remember(selectedFileUri) {
        mutableStateOf(
            selectedFileUri?.lastPathSegment?.substringAfterLast("/") ?: "File KRS Sebelumnya"
        )
    }
    // ----------------------------------------------

    val fakultasOptions = listOf("Pilih Fakultas") + fakultasList.map { it.nama }

    val isFormValid by derivedStateOf {
        name.isNotEmpty() &&
                nim.isNotEmpty() &&
                email.isNotEmpty() &&
                phone.isNotEmpty() &&
                selectedFakultas != "Pilih Fakultas" &&
                selectedJurusan != "Pilih Jurusan"
        // (Kita anggap KRS tidak wajib di-upload ulang)
    }

    var showSuccessBanner by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            // Nama file diperbarui oleh state 'remember(selectedFileUri)' di atas
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Edit Pendaftaran",
                onBack = { navController.popBackStack() }
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tampilkan error jika data tidak ditemukan
            if (existingData == null || event == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Data pendaftaran tidak ditemukan.", color = Color.Red)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tampilkan nama event dan tipe di atas form (di area cream)
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Text(
                        text = event.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    if (showSuccessBanner) {
                        RegistrationSuccessBanner(
                            // Tampilkan nama event yang benar
                            eventName = event.title,
                            successText = "Pendaftaran Event Diperbarui"
                        )
                    }

                    // Each input has its own white card (no big container)
                    FormInputField(
                        label = "Nama Lengkap",
                        value = name,
                        onValueChange = { name = it }
                    )
                    FormInputField(
                        label = "NIM",
                        value = nim,
                        onValueChange = { nim = it }
                    )
                    DropdownInput(
                        label = "Fakultas",
                        selectedOption = selectedFakultas,
                        options = fakultasOptions,
                        onOptionSelected = { newFakultas ->
                            selectedFakultas = newFakultas
                            selectedJurusan = "Pilih Jurusan"
                            availableJurusan =
                                fakultasList.find { it.nama == newFakultas }?.jurusan ?: emptyList()
                        }
                    )
                    DropdownInput(
                        label = "Jurusan",
                        selectedOption = selectedJurusan,
                        options = if (availableJurusan.isEmpty()) listOf(selectedJurusan) else (listOf("Pilih Jurusan") + availableJurusan),
                        onOptionSelected = { selectedJurusan = it },
                        enabled = availableJurusan.isNotEmpty()
                    )
                    FormInputField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it }
                    )
                    FormInputField(
                        label = "No Telepon",
                        value = phone,
                        onValueChange = { phone = it }
                    )
                    UploadKRSInput(
                        label = "KRS (Klik untuk mengganti)",
                        fileName = selectedFileName,
                        onUploadClick = { filePickerLauncher.launch("application/pdf") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PrimaryButton(
                        text = "Simpan Perubahan",
                        onClick = {
                            if (isFormValid && eventId != null) {
                                // --- SIMPAN DATA BARU KE VIEWMODEL ---
                                val updatedData = Registration( // <-- Perbaikan tipe
                                    eventId = eventId,
                                    name = name,
                                    nim = nim,
                                    fakultas = selectedFakultas,
                                    jurusan = selectedJurusan,
                                    email = email,
                                    phone = phone,
                                    krsUri = selectedFileUri.toString()
                                )
                                viewModel.updateRegistrationData(eventId, updatedData) // <-- Perbaikan tipe
                                showSuccessBanner = true
                                // -------------------------------------
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Auto-close success banner dan kembali ke layar sebelumnya
            LaunchedEffect(showSuccessBanner) {
                if (showSuccessBanner) {
                    delay(2000)
                    showSuccessBanner = false
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
fun RegistrationSuccessBanner(eventName: String, successText: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD0F5D3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = PrimaryGreen,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = eventName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = successText,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}