package com.example.uventapp.ui.screen.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource // Import
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown // Import Ikon Dropdown
import androidx.compose.material.icons.filled.Schedule // Import ikon jam
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
// Import yang diperlukan untuk Coil (AsyncImage)
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.R
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.data.model.Event
import com.example.uventapp.ui.theme.LightBackground
// --- IMPORT DUMMY EVENTS DARI LOKASI BARU ---
import com.example.uventapp.data.model.dummyEvents
import java.util.Calendar // Import untuk Kalender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(navController: NavController, viewModel: EventManagementViewModel) {
    // State untuk menyimpan data form
    var judul by remember { mutableStateOf("") }

    var jenis by remember { mutableStateOf("Pilih Jenis Event") }
    val jenisEventOptions = listOf("Seminar", "Talkshow", "Workshop", "Skill Lab")

    var tanggal by remember { mutableStateOf("") }
    var waktuMulai by remember { mutableStateOf("") }
    var waktuSelesai by remember { mutableStateOf("") }

    // --- PERBAIKAN: State untuk Lokasi ---
    var platformType by remember { mutableStateOf("Pilih Tipe Lokasi") }
    val platformOptions = listOf("Offline", "Online")
    var locationDetail by remember { mutableStateOf("") }
    // ----------------------------------

    var kuota by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher untuk memilih gambar dari galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // --- KALENDER ---
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

    // --- TIME PICKER (BARU) ---
    var showTimePicker by remember { mutableStateOf(false) }
    var isPickingStartTime by remember { mutableStateOf(true) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute) // Format HH:mm
            if (isPickingStartTime) {
                waktuMulai = formattedTime
            } else {
                waktuSelesai = formattedTime
            }
            showTimePicker = false
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true // true untuk format 24 jam
    )

    if (showTimePicker) {
        timePickerDialog.show()
        timePickerDialog.setOnDismissListener { showTimePicker = false }
    }
    // ---------------------------

    Scaffold(
        topBar = {
            CustomAppBar(title = "Tambah Event Baru", onBack = { navController.popBackStack() })
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Poster Event", fontWeight = FontWeight.SemiBold)
            PosterUploadBox(
                imageUri = imageUri,
                onClick = { galleryLauncher.launch("image/*") }
            )

            FormInputTextField(label = "Judul Event", value = judul, onValueChange = { judul = it })

            FormDropdownField(
                label = "Jenis Event",
                selectedValue = jenis,
                options = jenisEventOptions,
                onOptionSelected = { jenis = it }
            )

            FormInputTextField(
                label = "Tanggal Event",
                value = tanggal,
                onValueChange = { },
                placeholder = "DD/MM/YYYY",
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarToday, "Kalender")
                    }
                },
                readOnly = true
            )

            FormInputTextField(
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

            FormInputTextField(
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

            // Label dinamis berdasarkan pilihan dropdown
            val locationLabel = when (platformType) {
                "Online" -> "Link Meet (Zoom/GMeet)"
                "Offline" -> "Nama Lokasi (Gedung/Ruangan)"
                else -> "Lokasi/Link"
            }

            FormInputTextField(
                label = locationLabel,
                value = locationDetail,
                onValueChange = { locationDetail = it },
                // Tampilkan field ini hanya jika "Pilih Tipe Lokasi" *tidak* dipilih
                enabled = platformType != "Pilih Tipe Lokasi"
            )
            // ------------------------------------

            FormInputTextField(label = "Kuota", value = kuota, onValueChange = { kuota = it }, keyboardType = KeyboardType.Number)

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(text = "Simpan Event", onClick = {
                val maxDummyId = dummyEvents.maxOfOrNull { it.id } ?: 0
                val maxCreatedId = viewModel.createdEvents.value.maxOfOrNull { it.id } ?: 0
                val newId = maxOf(maxDummyId, maxCreatedId) + 1

                val newEvent = Event(
                    id = newId,
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
                    status = "Aktif",
                    thumbnailResId = if (imageUri == null) R.drawable.placeholder_poster else null,
                    thumbnailUri = imageUri?.toString()
                )

                viewModel.addEvent(newEvent)
                navController.popBackStack()
            })
        }
    }
}

@Composable
private fun PosterUploadBox(imageUri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                BorderStroke(2.dp, Color.LightGray),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.AddPhotoAlternate,
                    contentDescription = "Upload Poster",
                    tint = Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "+ Upload Poster",
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Klik untuk pilih dari galeri",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Poster Event",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormInputTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true, // Tambahkan enabled
    keyboardType: KeyboardType = KeyboardType.Text
) {

    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (placeholder != null) Text(placeholder) },
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            readOnly = readOnly,
            enabled = enabled, // Terapkan enabled
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

// --- COMPOSABLE HELPER BARU UNTUK DROPDOWN JENIS EVENT ---
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
                    .menuAnchor(), // Penting untuk ExposedDropdownMenu
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Pilih Jenis",
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
                // Tambahkan opsi "Pilih Jenis Event" jika itu adalah nilai default
                if (selectedValue == "Pilih Jenis Event" || selectedValue == "Pilih Tipe Lokasi") {
                    DropdownMenuItem(
                        text = { Text(selectedValue, color = Color.Gray) },
                        onClick = { expanded = false },
                        enabled = false
                    )
                }
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