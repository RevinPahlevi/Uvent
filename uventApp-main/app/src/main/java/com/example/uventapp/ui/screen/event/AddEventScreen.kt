package com.example.uventapp.ui.screen.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
// --- IMPORT PROFILE VIEW MODEL ---
import com.example.uventapp.ui.screen.profile.ProfileViewModel
// Removed dummy events import
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    profileViewModel: ProfileViewModel // <-- 1. Terima ViewModel
) {
    // (State judul, jenis, tanggal, dll. tetap sama)
    var judul by remember { mutableStateOf("") }
    var jenis by remember { mutableStateOf("Pilih Jenis Event") }
    val jenisEventOptions = listOf("Seminar", "Talkshow", "Workshop", "Skill Lab")
    // Tanggal kosong, user harus pilih dari DatePicker
    var tanggal by remember { mutableStateOf("") }
    var waktuMulai by remember { mutableStateOf("") }
    var waktuSelesai by remember { mutableStateOf("") }
    var platformType by remember { mutableStateOf("Pilih Tipe Lokasi") }
    val platformOptions = listOf("Offline", "Online")
    var locationDetail by remember { mutableStateOf("") }
    var kuota by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // --- Ambil ID user yang sedang login ---
    val currentUserProfile by profileViewModel.profile
    val currentUserId = currentUserProfile?.id
    // ------------------------------------

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    
    // --- VALIDASI WAKTU: State ---
    var selectedDateCalendar by remember { mutableStateOf<Calendar?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Helper: Cek apakah tanggal sama dengan hari ini
    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    
    var showDatePicker by remember { mutableStateOf(false) }

    // DatePicker dengan batasan minimum tanggal hari ini
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year: Int, month: Int, dayOfMonth: Int ->
            tanggal = "$dayOfMonth/${month + 1}/$year"
            selectedDateCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            // Reset waktu ketika tanggal berubah
            waktuMulai = ""
            waktuSelesai = ""
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
    
    // Show time picker with validation
    if (showTimePicker && selectedDateCalendar != null) {
        val now = Calendar.getInstance()
        val isToday = isSameDay(selectedDateCalendar!!, now)
        
        // Initial time
        val initHour = if (isPickingStartTime && isToday) now.get(Calendar.HOUR_OF_DAY) else 8
        val initMinute = if (isPickingStartTime && isToday) now.get(Calendar.MINUTE) else 0
        
        TimePickerDialog(
            context,
            { _, hour: Int, min: Int ->
                val time = String.format("%02d:%02d", hour, min)
                
                if (isPickingStartTime) {
                    if (isToday) {
                        val selectedM = hour * 60 + min
                        val currentM = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                        if (selectedM < currentM) {
                            // Set error, jangan set waktu
                            errorMessage = "Waktu mulai tidak boleh lebih awal dari sekarang"
                        } else {
                            waktuMulai = time
                            errorMessage = null
                        }
                    } else {
                        waktuMulai = time
                        errorMessage = null
                    }
                } else {
                    if (waktuMulai.isNotBlank()) {
                        val parts = waktuMulai.split(":")
                        val startM = parts[0].toInt() * 60 + parts[1].toInt()
                        val endM = hour * 60 + min
                        if (endM <= startM) {
                            // Set error, jangan set waktu
                            errorMessage = "Waktu selesai harus lebih lama dari waktu mulai"
                        } else {
                            waktuSelesai = time
                            errorMessage = null
                        }
                    } else {
                        waktuSelesai = time
                        errorMessage = null
                    }
                }
                showTimePicker = false
            },
            initHour,
            initMinute,
            true
        ).show()
    }

    Scaffold(
        topBar = {
            CustomAppBar(title = "Tambah Event Baru", onBack = { navController.popBackStack() })
        },
        containerColor = White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Poster Event Section
            FormCard {
                Text(
                    text = "Poster Event",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(12.dp))
                PosterUploadBox(
                    imageUri = imageUri,
                    onClick = { galleryLauncher.launch("image/*") }
                )
            }
            
            // Judul Event
            FormCard {
                FormInputTextField(
                    label = "Judul Event", 
                    value = judul, 
                    onValueChange = { judul = it },
                    placeholder = "Masukkan judul event"
                )
            }
            
            // Jenis Event
            FormCard {
                FormDropdownField(
                    label = "Jenis Event",
                    selectedValue = jenis,
                    options = jenisEventOptions,
                    onOptionSelected = { jenis = it },
                    placeholder = "Masukkan jenis event"
                )
            }
            
            // Tanggal Event
            FormCard {
                FormInputTextField(
                    label = "Tanggal Event",
                    value = tanggal,
                    onValueChange = { },
                    placeholder = "DD/MM/YYYY",
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.CalendarToday, "Kalender", tint = PrimaryGreen)
                        }
                    },
                    readOnly = true
                )
            }
            
            // Waktu
            FormCard {
                Text(
                    text = "Waktu",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = waktuMulai,
                        onValueChange = { },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Mulai", color = Color.Gray) },
                        trailingIcon = {
                            IconButton(onClick = {
                                isPickingStartTime = true
                                showTimePicker = true
                            }) {
                                Icon(Icons.Filled.Schedule, "Jam Mulai", tint = PrimaryGreen)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                    OutlinedTextField(
                        value = waktuSelesai,
                        onValueChange = { },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Selesai", color = Color.Gray) },
                        trailingIcon = {
                            IconButton(onClick = {
                                isPickingStartTime = false
                                showTimePicker = true
                            }) {
                                Icon(Icons.Filled.Schedule, "Jam Selesai", tint = PrimaryGreen)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }
                
                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
            }
            
            // Lokasi/Platform
            FormCard {
                FormDropdownField(
                    label = "Lokasi/Platform",
                    selectedValue = platformType,
                    options = platformOptions,
                    onOptionSelected = { platformType = it },
                    placeholder = "Pilih tipe lokasi"
                )
                
                if (platformType != "Pilih Tipe Lokasi") {
                    Spacer(modifier = Modifier.height(12.dp))
                    val locationLabel = when (platformType) {
                        "Online" -> "Link Meet"
                        "Offline" -> "Nama Lokasi (Gedung/Ruangan)"
                        else -> "Detail Lokasi"
                    }
                    val locationPlaceholder = when (platformType) {
                        "Online" -> "https://meet.google.com/xxx atau https://zoom.us/j/xxx"
                        "Offline" -> "Contoh: Gedung A Lantai 2, Ruang 201"
                        else -> "Masukkan lokasi event"
                    }
                    val locationKeyboardType = when (platformType) {
                        "Online" -> KeyboardType.Uri
                        else -> KeyboardType.Text
                    }
                    
                    Text(
                        text = locationLabel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = locationDetail,
                        onValueChange = { locationDetail = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(locationPlaceholder, color = Color.Gray, fontSize = 13.sp) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = false,
                        maxLines = 2,
                        keyboardOptions = KeyboardOptions(keyboardType = locationKeyboardType),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }
            
            // Kuota
            FormCard {
                FormInputTextField(
                    label = "Kuota", 
                    value = kuota, 
                    onValueChange = { kuota = it }, 
                    keyboardType = KeyboardType.Number,
                    placeholder = "Masukkan kuota event"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Validasi: Semua field harus diisi DAN tidak ada error
            val isFormValid = judul.isNotBlank() &&
                    jenis != "Pilih Jenis Event" &&
                    tanggal.isNotBlank() &&
                    waktuMulai.isNotBlank() &&
                    waktuSelesai.isNotBlank() &&
                    platformType != "Pilih Tipe Lokasi" &&
                    locationDetail.isNotBlank() &&
                    kuota.isNotBlank() &&
                    errorMessage == null // PENTING: Form tidak valid jika ada error

            // State untuk loading saat upload
            val isUploading by viewModel.isUploading

            // Simpan Button - Centered
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PrimaryButton(
                    text = if (isUploading) "Mengupload..." else "Simpan Event", 
                    onClick = {
                        // DEBUG: Log tanggal sebelum disimpan
                        android.util.Log.d("AddEventScreen", "=== SIMPAN EVENT ===")
                        android.util.Log.d("AddEventScreen", "Tanggal yang dipilih: $tanggal")
                        android.util.Log.d("AddEventScreen", "Waktu Mulai: $waktuMulai")
                        android.util.Log.d("AddEventScreen", "Waktu Selesai: $waktuSelesai")
                        
                        // Buat ID lokal baru sementara
                        val newId = (viewModel.allEvents.value.maxOfOrNull { it.id } ?: 0) + 1

                        // Fungsi untuk membuat dan mengirim event
                        fun createAndSendEvent(thumbnailUrl: String?) {
                            val newEvent = Event(
                                id = newId,
                                title = judul,
                                type = jenis,
                                date = tanggal,
                                timeStart = waktuMulai,
                                timeEnd = waktuSelesai,
                                platformType = platformType,
                                locationDetail = locationDetail,
                                quota = kuota,
                                status = "Aktif",
                                thumbnailResId = if (thumbnailUrl == null) R.drawable.placeholder_poster else null,
                                thumbnailUri = thumbnailUrl,
                                creatorId = currentUserId
                            )

                            viewModel.addEvent(newEvent, currentUserId, context)
                            navController.popBackStack()
                        }

                        // Jika ada gambar, upload dulu ke server
                        if (imageUri != null) {
                            viewModel.uploadImage(
                                context = context,
                                imageUri = imageUri!!,
                                onSuccess = { uploadedUrl ->
                                    android.util.Log.d("AddEventScreen", "Upload berhasil: $uploadedUrl")
                                    createAndSendEvent(uploadedUrl)
                                },
                                onError = { error ->
                                    android.util.Log.e("AddEventScreen", "Upload gagal: $error")
                                    // Tetap buat event tanpa gambar jika upload gagal
                                    createAndSendEvent(null)
                                }
                            )
                        } else {
                            // Tidak ada gambar, langsung buat event
                            createAndSendEvent(null)
                        }
                    },
                    enabled = isFormValid && !isUploading, // Tombol disabled saat upload
                    modifier = Modifier.width(200.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Form Card wrapper
@Composable
private fun FormCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE8E8E8))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

// Poster Upload Box with dashed green border
@Composable
private fun PosterUploadBox(imageUri: Uri?, onClick: () -> Unit) {
    val dashColor = PrimaryGreen
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f) // Standard portrait poster ratio
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0FFF0)) // Light green background
            .border(
                BorderStroke(2.dp, dashColor),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.AddPhotoAlternate,
                    contentDescription = "Upload Poster",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+Upload Poster",
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
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
                contentScale = ContentScale.Fit, // Fit to show full poster
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormInputTextField(
    label: String, value: String, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, placeholder: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null, readOnly: Boolean = false,
    enabled: Boolean = true, keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label, 
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color(0xFF333333),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (placeholder != null) Text(placeholder, color = Color.Gray) },
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            readOnly = readOnly,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDropdownField(
    label: String, selectedValue: String, options: List<String>,
    onOptionSelected: (String) -> Unit,
    placeholder: String = ""
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = if (selectedValue.startsWith("Pilih")) "" else selectedValue

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color(0xFF333333),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = displayValue,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                placeholder = { Text(placeholder, color = Color.Gray) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Pilih",
                        tint = PrimaryGreen,
                        modifier = Modifier.clickable { expanded = true }
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
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

