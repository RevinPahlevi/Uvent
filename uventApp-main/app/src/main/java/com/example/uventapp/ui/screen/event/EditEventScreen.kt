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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.uventapp.R
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.data.model.Event
import com.example.uventapp.ui.theme.LightBackground
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    eventId: Int?
) {
    // Ambil data event yang akan diedit
    val event = eventId?.let { viewModel.getEventById(it) }

    // State untuk form (diisi dengan data event yang ada)
    var judul by remember { mutableStateOf(event?.title ?: "") }
    var jenis by remember { mutableStateOf(event?.type ?: "Pilih Jenis Event") }
    val jenisEventOptions = listOf("Seminar", "Talkshow", "Workshop", "Skill Lab")
    var tanggal by remember { mutableStateOf(event?.date ?: "") }
    var waktuMulai by remember { mutableStateOf(event?.timeStart ?: "") }
    var waktuSelesai by remember { mutableStateOf(event?.timeEnd ?: "") }
    var platformType by remember { mutableStateOf(event?.platformType ?: "Pilih Tipe Lokasi") }
    val platformOptions = listOf("Offline", "Online")
    var locationDetail by remember { mutableStateOf(event?.locationDetail ?: "") }
    var kuota by remember { mutableStateOf(event?.quota ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(event?.thumbnailUri?.let { Uri.parse(it) }) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

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
    ).apply {
        // Set minimum date ke hari ini (tidak bisa pilih tanggal lampau)
        datePicker.minDate = System.currentTimeMillis() - 1000
    }
    if (showDatePicker) {
        datePickerDialog.show()
        datePickerDialog.setOnDismissListener { showDatePicker = false }
    }

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
        true
    )
    if (showTimePicker) {
        timePickerDialog.show()
        timePickerDialog.setOnDismissListener { showTimePicker = false }
    }

    Scaffold(
        topBar = {
            CustomAppBar(title = "Edit Event", onBack = { navController.popBackStack() })
        },
        containerColor = LightBackground
    ) { paddingValues ->
        if (event == null) {
            // Jika event tidak ditemukan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Event tidak ditemukan", color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Poster Event", fontWeight = FontWeight.SemiBold)
                EditPosterUploadBox(
                    imageUri = imageUri,
                    existingResId = event.thumbnailResId,
                    onClick = { galleryLauncher.launch("image/*") }
                )

                EditFormInputTextField(
                    label = "Judul Event",
                    value = judul,
                    onValueChange = { judul = it }
                )

                EditFormDropdownField(
                    label = "Jenis Event",
                    selectedValue = jenis,
                    options = jenisEventOptions,
                    onOptionSelected = { jenis = it }
                )

                EditFormInputTextField(
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

                EditFormInputTextField(
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

                EditFormInputTextField(
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

                EditFormDropdownField(
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
                EditFormInputTextField(
                    label = locationLabel,
                    value = locationDetail,
                    onValueChange = { locationDetail = it },
                    enabled = platformType != "Pilih Tipe Lokasi"
                )

                EditFormInputTextField(
                    label = "Kuota",
                    value = kuota,
                    onValueChange = { kuota = it },
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(text = "Simpan Perubahan", onClick = {
                    val updatedEvent = event.copy(
                        title = judul,
                        type = jenis,
                        date = tanggal,
                        timeStart = waktuMulai,
                        timeEnd = waktuSelesai,
                        platformType = platformType,
                        locationDetail = locationDetail,
                        quota = kuota,
                        thumbnailUri = imageUri?.toString() ?: event.thumbnailUri
                    )
                    viewModel.updateEvent(updatedEvent, context)
                    navController.popBackStack()
                })
            }
        }
    }
}

@Composable
private fun EditPosterUploadBox(
    imageUri: Uri?,
    existingResId: Int?,
    onClick: () -> Unit
) {
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
        when {
            imageUri != null -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Poster Event",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            existingResId != null -> {
                Image(
                    painter = painterResource(id = existingResId),
                    contentDescription = "Poster Event",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditFormInputTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
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
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditFormDropdownField(
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
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
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
