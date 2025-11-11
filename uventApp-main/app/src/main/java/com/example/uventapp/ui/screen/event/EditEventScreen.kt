package com.example.uventapp.ui.screen.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown // Import
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.uventapp.R
import com.example.uventapp.data.model.dummyEvents // Import
import com.example.uventapp.ui.components.BottomNavBar
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.theme.DisabledBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    eventId: Int?
) {
    val eventToEdit = remember(eventId) {
        eventId?.let {
            // Cari di semua list
            (dummyEvents + viewModel.createdEvents.value).find { it.id == eventId }
        }
    }

    if (eventToEdit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Event tidak ditemukan atau ID salah.")
        }
        return
    }

    // Isi state form dengan data yang sudah ada
    var judul by remember { mutableStateOf(eventToEdit.title) }

    // --- PERBAIKAN: State untuk Dropdown Jenis Event ---
    var jenis by remember { mutableStateOf(eventToEdit.type) } // Ambil dari data
    val jenisEventOptions = listOf("Seminar", "Talkshow", "Workshop", "Skill Lab")
    // -------------------------------------------------

    var tanggal by remember { mutableStateOf(eventToEdit.date) }
    var waktuMulai by remember { mutableStateOf(eventToEdit.timeStart) }
    var waktuSelesai by remember { mutableStateOf(eventToEdit.timeEnd) }

    // --- PERBAIKAN: State untuk Lokasi ---
    var platformType by remember { mutableStateOf(eventToEdit.platformType) }
    val platformOptions = listOf("Offline", "Online")
    var locationDetail by remember { mutableStateOf(eventToEdit.locationDetail) }
    // ----------------------------------

    var kuota by remember { mutableStateOf(eventToEdit.quota) }
    var posterUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        posterUri = uri
    }

    // --- LOGIKA UNTUK DATE PICKER (KALENDER) ---
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year: Int, month: Int, dayOfMonth: Int ->
            tanggal = "$dayOfMonth/${month + 1}/$year"
            showDatePicker = false
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    if (showDatePicker) {
        datePickerDialog.show()
        datePickerDialog.setOnDismissListener { showDatePicker = false }
    }
    // ---------------------------------------------

    // --- LOGIKA UNTUK TIME PICKER (JAM) ---
    var showTimePicker by remember { mutableStateOf(false) }
    var isPickingStartTime by remember { mutableStateOf(true) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            if (isPickingStartTime) {
                waktuMulai = formattedTime
            } else {
                waktuSelesai = formattedTime
            }
            showTimePicker = false
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true // 24 jam
    )

    if (showTimePicker) {
        timePickerDialog.show()
        timePickerDialog.setOnDismissListener { showTimePicker = false }
    }
    // ---------------------------------------

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Edit Event",
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            UploadPosterField(
                isEditing = true,
                posterUri = posterUri,
                onClick = { imagePickerLauncher.launch("image/*") }
            )

            // Input Fields
            EventInputField(
                label = "Judul Event",
                value = judul,
                onValueChange = { judul = it }
            )

            // --- PERBAIKAN: Ganti Input Teks jadi Dropdown ---
            FormDropdownField(
                label = "Jenis Event",
                selectedValue = jenis,
                options = jenisEventOptions,
                onOptionSelected = { jenis = it }
            )
            // ------------------------------------------------

            EventInputField(
                label = "Tanggal Event",
                value = tanggal,
                onValueChange = { },
                placeholder = "DD/MM/YYYY",
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = "Kalender")
                    }
                },
                readOnly = true
            )

            EventInputField(
                label = "Waktu Mulai",
                value = waktuMulai,
                onValueChange = { },
                placeholder = "HH:MM",
                trailingIcon = {
                    IconButton(onClick = {
                        isPickingStartTime = true
                        showTimePicker = true
                    }) {
                        Icon(Icons.Filled.Schedule, "Jam Mulai")
                    }
                },
                readOnly = true
            )

            EventInputField(
                label = "Waktu Selesai",
                value = waktuSelesai,
                onValueChange = { },
                placeholder = "HH:MM",
                trailingIcon = {
                    IconButton(onClick = {
                        isPickingStartTime = false
                        showTimePicker = true
                    }) {
                        Icon(Icons.Filled.Schedule, "Jam Selesai")
                    }
                },
                readOnly = true
            )

            // --- PERBAIKAN: Ganti Input Lokasi ---
            FormDropdownField(
                label = "Tipe Lokasi",
                selectedValue = platformType,
                options = platformOptions,
                onOptionSelected = { platformType = it }
            )

            val locationLabel = when (platformType) {
                "Online" -> "Link Meet (Zoom/GMeet)"
                "Offline" -> "Nama Lokasi (Gedung/Ruangan)"
                else -> "Lokasi/Link"
            }

            EventInputField(
                label = locationLabel,
                value = locationDetail,
                onValueChange = { locationDetail = it },
                enabled = platformType.isNotEmpty() && platformType != "Pilih Tipe Lokasi"
            )
            // ------------------------------------

            EventInputField(
                label = "Kuota",
                value = kuota,
                onValueChange = { kuota = it },
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(text = "Simpan Perubahan", onClick = {
                val updatedEvent = eventToEdit.copy(
                    title = judul,
                    type = jenis,
                    date = tanggal,
                    timeStart = waktuMulai,
                    timeEnd = waktuSelesai,

                    // --- PERBAIKAN: Simpan data lokasi baru ---
                    platformType = platformType,
                    locationDetail = locationDetail,
                    // ----------------------------------------

                    quota = kuota,
                    thumbnailResId = if (posterUri != null) null else eventToEdit.thumbnailResId,
                    thumbnailUri = posterUri?.toString() ?: eventToEdit.thumbnailUri
                )

                viewModel.updateEvent(updatedEvent)
                navController.popBackStack()
            })

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Composable kustom untuk field input form
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true // Tambahkan enabled
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            placeholder = { if (placeholder != null) Text(placeholder, color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors( // --- Ganti ke OutlinedTextFieldDefaults ---
                focusedContainerColor = DisabledBackground,
                unfocusedContainerColor = DisabledBackground,
                disabledContainerColor = DisabledBackground,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = PrimaryGreen
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = trailingIcon,
            readOnly = readOnly,
            enabled = enabled, // Terapkan enabled
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Composable untuk area upload poster (versi Edit)
 */
@Composable
private fun UploadPosterField(
    isEditing: Boolean,
    posterUri: Uri?,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Poster Event", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(
                    width = 2.dp,
                    color = PrimaryGreen,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = onClick)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (posterUri == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (isEditing) Icons.Filled.Edit else Icons.Filled.Edit,
                        contentDescription = "Upload",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = if (isEditing) "Ganti Poster" else "+ Upload Poster",
                        color = PrimaryGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Klik untuk pilih dari galeri",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } else {
                Text(
                    text = "Gambar Baru Dipilih: ${posterUri.lastPathSegment}",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// --- COMPOSABLE HELPER BARU UNTUK DROPDOWN (dicopy dari AddEventScreen) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Pilih",
                        modifier = Modifier.clickable { expanded = true }
                    )
                },
                shape = RoundedCornerShape(8.dp),
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                // Jangan tampilkan opsi "Pilih..." di dalam menu
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}