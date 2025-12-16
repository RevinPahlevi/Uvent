package com.example.uventapp.ui.screen.registration

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.uventapp.data.model.fakultasList
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.LightBackground
// Import ViewModel dan data
// Removed dummy events import
// --- PERBAIKAN: IMPORT Registration, BUKAN RegistrationData ---
import com.example.uventapp.data.model.Registration
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.data.network.ApiClient
import com.example.uventapp.data.network.CheckNimResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ---------------------------
// Helper Composables (JANGAN HAPUS)
// ---------------------------

@Composable
fun FormInputField(
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    // Individual white card for each input
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Label INSIDE the card
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            // Gray TextField with LARGER radius than card
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8E8E8),
                    unfocusedContainerColor = Color(0xFFE8E8E8),
                    disabledContainerColor = Color(0xFFE8E8E8),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                shape = RoundedCornerShape(12.dp),  // Larger than card (12dp > 8dp)
                placeholder = { Text("") },
                singleLine = true
            )
        }
    }
}

@Composable
fun DropdownInput(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    // Individual white card for each dropdown
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Label INSIDE the card
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            // Gray TextField with LARGER radius than card
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.clickable { if (enabled) expanded = !expanded }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { if (enabled) expanded = !expanded },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFE8E8E8),
                    unfocusedContainerColor = Color(0xFFE8E8E8),
                    disabledContainerColor = Color(0xFFD0D0D0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                shape = RoundedCornerShape(12.dp),  // Larger than card (12dp > 8dp)
                singleLine = true
            )
        }
    }
    DropdownMenu(
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

@Composable
fun UploadKRSInput(label: String, fileName: String, onUploadClick: () -> Unit) {
    val hasFile = fileName.isNotEmpty() && fileName != "Tidak ada file dipilih"

    // Individual white card for upload button
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Label INSIDE the card
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Gray background box with LARGER radius
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8E8E8), RoundedCornerShape(12.dp))
                    .clickable { onUploadClick() }
                    .padding(12.dp)
            ) {
                if (hasFile) {
                    // Tampilkan nama file dengan icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = "File uploaded",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = fileName,
                                fontSize = 13.sp,
                                color = Color.Black,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "Ubah",
                            fontSize = 12.sp,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Tampilkan button upload
                    PrimaryButton(
                        text = "Upload File",
                        onClick = onUploadClick,
                        modifier = Modifier.width(140.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------
// Main Registration Form
// ---------------------------

@SuppressLint("UnrememberedMutableState")
@Composable
fun RegistrationFormScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    profileViewModel: com.example.uventapp.ui.screen.profile.ProfileViewModel,
    eventId: Int?
) {
    // Context untuk API call
    val context = LocalContext.current

    // Ambil userId dari profile
    val currentUserProfile by profileViewModel.profile
    val currentUserId = currentUserProfile?.id

    // Cari event berdasarkan ID dari semua event yang ada
    val eventToRegister = remember(eventId, viewModel.allEvents.value, viewModel.createdEvents.value) {
        (viewModel.allEvents.value + viewModel.createdEvents.value).find { it.id == eventId }
    }

    // AUTO-FILL: Inisialisasi field dengan data dari profile user yang login
    var name by remember(currentUserProfile) { 
        mutableStateOf(currentUserProfile?.name ?: "") 
    }
    var nim by remember { mutableStateOf("") }
    var nimError by remember { mutableStateOf<String?>(null) }
    var selectedFakultas by remember { mutableStateOf("Pilih Fakultas") }
    var selectedJurusan by remember { mutableStateOf("Pilih Jurusan") }
    var availableJurusan by remember { mutableStateOf(listOf<String>()) }
    // AUTO-FILL: Email dan Phone dari profile user
    var email by remember(currentUserProfile) { 
        mutableStateOf(currentUserProfile?.email ?: "") 
    }
    var phone by remember(currentUserProfile) { 
        mutableStateOf(currentUserProfile?.phone ?: "") 
    }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("Tidak ada file dipilih") }

    // State untuk tracking pendaftaran
    var isRegistering by remember { mutableStateOf(false) }

    // Observe notification message untuk handle error
    val notificationMessage by viewModel.notificationMessage
    LaunchedEffect(notificationMessage) {
        if (isRegistering && notificationMessage != null) {
            when {
                notificationMessage!!.contains("Berhasil", ignoreCase = true) -> {
                    // Sukses - navigasi ke Event Saya
                    eventToRegister?.let { event ->
                        navController.navigate(Screen.MyRegisteredEvent.createRoute(event.title)) {
                            popUpTo(Screen.Home.route)
                            launchSingleTop = true
                        }
                    }
                    isRegistering = false
                }
                notificationMessage!!.contains("NIM", ignoreCase = true) -> {
                    // Error NIM
                    nimError = "Masukkan NIM anda dengan benar"
                    isRegistering = false
                }
                else -> {
                    // Error lain (kuota penuh, sudah terdaftar, dll)
                    isRegistering = false
                }
            }
            viewModel.clearNotification()
        }
    }

    val fakultasOptions = listOf("Pilih Fakultas") + fakultasList.map { it.nama }

    val isFormValid by derivedStateOf {
        name.isNotEmpty() &&
                nim.isNotEmpty() &&
                email.isNotEmpty() &&
                phone.isNotEmpty() &&
                selectedFakultas != "Pilih Fakultas" &&
                selectedJurusan != "Pilih Jurusan" &&
                selectedFileUri != null &&
                eventToRegister != null // Pastikan event ada
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = it.lastPathSegment?.substringAfterLast("/") ?: "File dipilih"
        }
    }

    Scaffold(
        topBar = { CustomAppBar(title = "Daftar", onBack = { navController.popBackStack() }) },
        containerColor = LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tampilkan nama event di atas form (di area cream)
            if (eventToRegister != null) {
                Text(
                    text = eventToRegister.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Text(
                    text = eventToRegister.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            FormInputField(
                label = "Nama Lengkap",
                value = name,
                onValueChange = { name = it }
            )
            FormInputField(
                label = "NIM",
                value = nim,
                onValueChange = { newValue ->
                    // Hanya terima angka atau string kosong (untuk delete)
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        nim = newValue
                        nimError = null // Clear error saat user mengetik
                    }
                },
                keyboardType = KeyboardType.Number
            )

            // Warning NIM error dari main branch
            if (nimError != null) {
                Text(
                    text = nimError!!,
                    color = Color(0xFFC62828),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 2.dp)
                )
            }

            DropdownInput(
                label = "Fakultas",
                selectedOption = selectedFakultas,
                options = fakultasOptions,
                onOptionSelected = { newFakultas ->
                    selectedFakultas = newFakultas
                    selectedJurusan = "Pilih Jurusan"
                    availableJurusan = fakultasList.find { it.nama == newFakultas }?.jurusan.orEmpty()
                }
            )

            DropdownInput(
                label = "Jurusan",
                selectedOption = selectedJurusan,
                options = availableJurusan.ifEmpty { listOf("Pilih Jurusan") },
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
                onValueChange = { newValue ->
                    // Hanya terima angka atau string kosong (untuk delete)
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        phone = newValue
                    }
                },
                keyboardType = KeyboardType.Phone
            )

            UploadKRSInput(
                label = "KRS",
                fileName = selectedFileName,
                onUploadClick = { filePickerLauncher.launch("application/pdf") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // DEBUG: Show validation status
            if (!isFormValid) {
                Text(
                    text = "Form belum lengkap: " +
                            "name=${name.isNotEmpty()}, " +
                            "nim=${nim.isNotEmpty()}, " +
                            "email=${email.isNotEmpty()}, " +
                            "phone=${phone.isNotEmpty()}, " +
                            "fakultas=${selectedFakultas != "Pilih Fakultas"}, " +
                            "jurusan=${selectedJurusan != "Pilih Jurusan"}, " +
                            "file=${selectedFileUri != null}",
                    fontSize = 10.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }

            PrimaryButton(
                text = if (viewModel.isUploading.value) "Mengupload..." else "Daftar",
                onClick = {
                    android.util.Log.d("RegistrationForm", "Button clicked! isFormValid=$isFormValid")
                    if (isFormValid && selectedFileUri != null) {
                        // COMBINED FLOW: 1. Check NIM, 2. Upload KRS, 3. Register
                        isRegistering = true
                        
                        // Step 1: Cek NIM terlebih dahulu dari main branch
                        ApiClient.instance.checkNimExists(eventToRegister!!.id, nim)
                            .enqueue(object : Callback<CheckNimResponse> {
                                override fun onResponse(
                                    call: Call<CheckNimResponse>,
                                    response: Response<CheckNimResponse>
                                ) {
                                    val body = response.body()
                                    if (response.isSuccessful && body?.status == "success") {
                                        if (body.data?.exists == true) {
                                            // NIM sudah terdaftar - tampilkan pesan error
                                            nimError = "Masukkan NIM anda dengan benar"
                                            isRegistering = false
                                        } else {
                                            // NIM belum terdaftar - lanjut upload KRS
                                            proceedWithRegistration()
                                        }
                                    } else {
                                        // Error dari API - lanjut saja
                                        proceedWithRegistration()
                                    }
                                }

                                override fun onFailure(call: Call<CheckNimResponse>, t: Throwable) {
                                    // Jika check gagal, lanjut saja
                                    proceedWithRegistration()
                                }
                                
                                fun proceedWithRegistration() {
                                    // Step 2: Upload file KRS ke server (dari loly branch)
                                    android.util.Log.d("RegistrationForm", "Starting KRS upload...")
                                    viewModel.uploadKRS(
                                        context = context,
                                        krsUri = selectedFileUri!!,
                                        onSuccess = { krsUrl ->
                                            android.util.Log.d("RegistrationForm", "KRS uploaded! URL: $krsUrl")
                                            
                                            // Step 3: Submit registrasi dengan KRS URL
                                            val registrationData = Registration(
                                                eventId = eventToRegister.id,
                                                name = name,
                                                nim = nim,
                                                fakultas = selectedFakultas,
                                                jurusan = selectedJurusan,
                                                email = email,
                                                phone = phone,
                                                krsUri = krsUrl
                                            )
                                            
                                            android.util.Log.d("RegistrationForm", "Calling registerForEvent with KRS URL: $krsUrl")
                                            viewModel.registerForEvent(
                                                event = eventToRegister,
                                                data = registrationData,
                                                userId = currentUserId,
                                                context = context,
                                                onSuccess = {
                                                    android.util.Log.d("RegistrationForm", "Registration success! Navigating...")
                                                    navController.navigate(Screen.MyRegisteredEvent.createRoute(eventToRegister.title)) {
                                                        popUpTo(Screen.Home.route)
                                                        launchSingleTop = true
                                                    }
                                                },
                                                onError = { errorMessage ->
                                                    android.util.Log.e("RegistrationForm", "Registration error: $errorMessage")
                                                    isRegistering = false
                                                }
                                            )
                                        },
                                        onError = { errorMessage ->
                                            android.util.Log.e("RegistrationForm", "KRS upload error: $errorMessage")
                                            isRegistering = false
                                        }
                                    )
                                }
                            })
                    } else {
                        android.util.Log.w("RegistrationForm", "Form not valid or no file selected")
                    }
                },
                enabled = isFormValid && !viewModel.isUploading.value,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}