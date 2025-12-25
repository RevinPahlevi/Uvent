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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.uventapp.data.model.fakultasList
import com.example.uventapp.data.model.Registration
import com.example.uventapp.ui.screen.event.EventManagementViewModel
import com.example.uventapp.data.network.ApiClient
import com.example.uventapp.data.network.CheckNimResponse
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.uventapp.ui.components.*
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.White
import com.example.uventapp.data.network.UpdateRegistrationRequest
import com.example.uventapp.data.network.UpdateRegistrationResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRegistrationScreen(
    navController: NavController,
    viewModel: EventManagementViewModel,
    eventId: Int?
) {
    val context = LocalContext.current
    
    val event = remember(eventId, viewModel.allEvents.value, viewModel.createdEvents.value, viewModel.followedEvents) {
        (viewModel.allEvents.value + viewModel.createdEvents.value + viewModel.followedEvents).find { it.id == eventId }
    }
    val existingData = remember(eventId) {
        viewModel.getRegistrationData(eventId ?: -1)
    }

    var name by remember { mutableStateOf(existingData?.name ?: "") }
    var nim by remember { mutableStateOf(existingData?.nim ?: "") }
    var selectedFakultas by remember { mutableStateOf(existingData?.fakultas ?: "Pilih Fakultas") }
    var selectedJurusan by remember { mutableStateOf(existingData?.jurusan ?: "Pilih Jurusan") }

    var availableJurusan by remember(selectedFakultas) {
        mutableStateOf(fakultasList.find { it.nama == selectedFakultas }?.jurusan ?: emptyList())
    }

    var email by remember { mutableStateOf(existingData?.email ?: "") }
    var phone by remember { mutableStateOf(existingData?.phone ?: "") }

    var selectedFileUri by remember { mutableStateOf(existingData?.krsUri?.toUri()) }
    var selectedFileName by remember(selectedFileUri) {
        mutableStateOf(
            if (selectedFileUri != null) {
                selectedFileUri?.lastPathSegment?.substringAfterLast("/") ?: "KRS.pdf"
            } else if (existingData?.krsUri != null) {
                existingData.krsUri?.substringAfterLast("/") ?: "KRS.pdf"
            } else {
                "KRS.pdf"
            }
        )
    }

    val fakultasOptions = listOf("Pilih Fakultas") + fakultasList.map { it.nama }

    val isFormValid by derivedStateOf {
        name.isNotEmpty() &&
                nim.isNotEmpty() &&
                email.isNotEmpty() &&
                phone.isNotEmpty() &&
                selectedFakultas != "Pilih Fakultas" &&
                selectedJurusan != "Pilih Jurusan"
    }
    
    var nimErrorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    
    var showNoChangeDialog by remember { mutableStateOf(false) }
    var showConfirmSaveDialog by remember { mutableStateOf(false) }
    
    val hasChanges by remember(name, nim, selectedFakultas, selectedJurusan, email, phone, selectedFileUri) {
        derivedStateOf {
            name != (existingData?.name ?: "") ||
            nim != (existingData?.nim ?: "") ||
            selectedFakultas != (existingData?.fakultas ?: "Pilih Fakultas") ||
            selectedJurusan != (existingData?.jurusan ?: "Pilih Jurusan") ||
            email != (existingData?.email ?: "") ||
            phone != (existingData?.phone ?: "") ||
            selectedFileUri?.toString() != existingData?.krsUri
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        selectedFileName = uri?.lastPathSegment ?: "No file chosen"
    }
    
    LaunchedEffect(nim, existingData) {
        if (eventId != null && nim.isNotEmpty()) {
            val originalNim = existingData?.nim ?: ""
            if (nim != originalNim) {
                try {
                    withContext(Dispatchers.IO) {
                        val response = ApiClient.instance.checkNimExists(eventId, nim).execute()
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful && response.body()?.data?.exists == true) {
                                nimErrorMessage = "NIM yang anda masukkan sudah terdaftar pada event ini"
                            } else {
                                nimErrorMessage = null
                            }
                        }
                    }
                } catch (e: Exception) {
                    nimErrorMessage = null
                }
            } else {
                nimErrorMessage = null
            }
        } else if (nim.isEmpty()) {
            nimErrorMessage = null
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Edit Pendaftaran",
                onBack = {
                    navController.navigate(Screen.MyRegisteredEvent.createRoute("_return_to_followed")) {
                        popUpTo(Screen.MyRegisteredEvent.route) { inclusive = true }
                    }
                }
            )
        },
        containerColor = LightBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                            eventName = event.title,
                            successText = "Pendaftaran Event Diperbarui"
                        )
                    }

                    ReadOnlyFormField(
                        label = "Nama Lengkap",
                        value = name
                    )
                    FormInputField(
                        label = "NIM",
                        value = nim,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                                nim = newValue
                                nimErrorMessage = null
                            }
                        },
                        keyboardType = KeyboardType.Number
                    )
                    nimErrorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
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
                    ReadOnlyFormField(
                        label = "Email",
                        value = email
                    )
                    ReadOnlyFormField(
                        label = "No Telepon",
                        value = phone
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
                            if (isFormValid && eventId != null && nimErrorMessage == null) {
                                if (!hasChanges) {
                                    showNoChangeDialog = true
                                } else {
                                    showConfirmSaveDialog = true
                                }
                            }
                        },
                        enabled = isFormValid && nimErrorMessage == null,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            
            if (showNoChangeDialog) {
                NoChangeInfoDialog(
                    onDismiss = { showNoChangeDialog = false },
                    onContinueEdit = { showNoChangeDialog = false },
                    onOke = {
                        showNoChangeDialog = false
                        navController.navigate(Screen.MyRegisteredEvent.createRoute("_no_change")) {
                            popUpTo(Screen.MyRegisteredEvent.route) { inclusive = true }
                        }
                    }
                )
            }
            
            if (showConfirmSaveDialog && eventId != null) {
                ConfirmSaveDialog(
                    onDismiss = { showConfirmSaveDialog = false },
                    onConfirm = {
                        showConfirmSaveDialog = false
                        saveRegistrationData(
                            context = context,
                            eventId = eventId,
                            name = name,
                            nim = nim,
                            fakultas = selectedFakultas,
                            jurusan = selectedJurusan,
                            email = email,
                            phone = phone,
                            krsUri = selectedFileUri,
                            originalKrsUrl = existingData?.krsUri,
                            viewModel = viewModel,
                            onSuccess = {
                                navController.navigate(Screen.MyRegisteredEvent.createRoute("_edit_success")) {
                                    popUpTo(Screen.MyRegisteredEvent.route) { inclusive = true }
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}

private fun saveRegistrationData(
    context: android.content.Context,
    eventId: Int,
    name: String,
    nim: String,
    fakultas: String,
    jurusan: String,
    email: String,
    phone: String,
    krsUri: Uri?,
    originalKrsUrl: String?,
    viewModel: EventManagementViewModel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit = {}
) {
    android.util.Log.d("EditRegistration", "=== SAVE REGISTRATION DATA ===")
    android.util.Log.d("EditRegistration", "EventId: $eventId")
    
    val registrationData = viewModel.getRegistrationData(eventId)
    android.util.Log.d("EditRegistration", "Registration data: $registrationData")
    
    val registrationId = registrationData?.registrationId
    if (registrationId == null) {
        android.util.Log.e("EditRegistration", "❌ REGISTRATION ID IS NULL! Cannot save.")
        onError("Registration ID tidak ditemukan")
        return
    }
    
    android.util.Log.d("EditRegistration", "RegistrationId: $registrationId")
    android.util.Log.d("EditRegistration", "Original KRS URL: $originalKrsUrl")
    android.util.Log.d("EditRegistration", "Current KRS URI: $krsUri")
    android.util.Log.d("EditRegistration", "KRS URI toString: ${krsUri?.toString()}")
    
    val isNewFile = krsUri != null
    val isDifferentFromOriginal = krsUri?.toString() != originalKrsUrl
    val isContentUri = krsUri?.toString()?.startsWith("content://") == true
    
    android.util.Log.d("EditRegistration", "isNewFile: $isNewFile")
    android.util.Log.d("EditRegistration", "isDifferentFromOriginal: $isDifferentFromOriginal")
    android.util.Log.d("EditRegistration", "isContentUri: $isContentUri")
    
    val krsChanged = isNewFile && isDifferentFromOriginal && isContentUri
    android.util.Log.d("EditRegistration", "krsChanged: $krsChanged")
    
    if (krsChanged) {
        android.util.Log.d("EditRegistration", ">>> UPLOADING NEW KRS FILE...")
        viewModel.uploadKRS(
            context = context,
            krsUri = krsUri!!,
            onSuccess = { uploadedKrsUrl ->
                android.util.Log.d("EditRegistration", "<<< KRS UPLOADED: $uploadedKrsUrl")
                doUpdateRegistration(registrationId, eventId, name, nim, fakultas, jurusan, email, phone, uploadedKrsUrl, viewModel, onSuccess, onError)
            },
            onError = { errorMessage ->
                android.util.Log.e("EditRegistration", "<<< KRS UPLOAD FAILED: $errorMessage")
                onError("Gagal upload KRS: $errorMessage")
            }
        )
    } else {
        val krsUrlToSave = originalKrsUrl ?: krsUri?.toString()
        android.util.Log.d("EditRegistration", ">>> NO KRS CHANGE, using: $krsUrlToSave")
        doUpdateRegistration(registrationId, eventId, name, nim, fakultas, jurusan, email, phone, krsUrlToSave, viewModel, onSuccess, onError)
    }
}

private fun doUpdateRegistration(
    registrationId: Int,
    eventId: Int,
    name: String,
    nim: String,
    fakultas: String,
    jurusan: String,
    email: String,
    phone: String,
    krsUrl: String?,
    viewModel: EventManagementViewModel,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val updateRequest = UpdateRegistrationRequest(
        name = name,
        nim = nim,
        fakultas = fakultas,
        jurusan = jurusan,
        email = email,
        phone = phone,
        krsUri = krsUrl
    )
    
    android.util.Log.d("EditRegistration", "Calling API updateRegistration with ID: $registrationId")
    
    ApiClient.instance.updateRegistration(registrationId, updateRequest).enqueue(object : Callback<UpdateRegistrationResponse> {
        override fun onResponse(call: Call<UpdateRegistrationResponse>, response: Response<UpdateRegistrationResponse>) {
            android.util.Log.d("EditRegistration", "API Response code: ${response.code()}")
            if (response.isSuccessful) {
                android.util.Log.d("EditRegistration", "✅ UPDATE SUCCESS!")
                val updatedData = Registration(
                    eventId = eventId,
                    name = name,
                    nim = nim,
                    fakultas = fakultas,
                    jurusan = jurusan,
                    email = email,
                    phone = phone,
                    krsUri = krsUrl,
                    registrationId = registrationId
                )
                viewModel.updateRegistrationData(eventId, updatedData)
                onSuccess()
            } else {
                android.util.Log.e("EditRegistration", "❌ API Error: ${response.code()} - ${response.message()}")
                android.util.Log.e("EditRegistration", "Error body: ${response.errorBody()?.string()}")
                onError("Gagal menyimpan: ${response.message()}")
            }
        }
        override fun onFailure(call: Call<UpdateRegistrationResponse>, t: Throwable) {
            android.util.Log.e("EditRegistration", "❌ API CALL FAILED: ${t.message}", t)
            onError("Koneksi gagal: ${t.message}")
        }
    })
}

@Composable
private fun NoChangeInfoDialog(
    onDismiss: () -> Unit,
    onContinueEdit: () -> Unit,
    onOke: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "Tidak Ada Perubahan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Anda belum melakukan perubahan apa pun pada data pendaftaran.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onContinueEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen,
                            contentColor = White
                        )
                    ) {
                        Text("Lanjut Edit")
                    }
                    Button(
                        onClick = onOke,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            contentColor = White
                        )
                    ) {
                        Text("Oke")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmSaveDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "Simpan perubahan?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Apakah anda yakin untuk menyimpan perubahan data pendaftaran ini?",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
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
                            containerColor = PrimaryGreen,
                            contentColor = White
                        )
                    ) {
                        Text("Ya")
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            contentColor = White
                        )
                    ) {
                        Text("Tidak")
                    }
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