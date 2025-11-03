package com.example.uventapp.ui.screen.registration

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.navigation.NavController
import com.example.uventapp.data.model.fakultasList
import com.example.uventapp.ui.components.*
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRegistrationScreen(navController: NavController, eventName: String) {
    var name by remember { mutableStateOf("Nama Contoh") }
    var nim by remember { mutableStateOf("123456789") }
    var selectedFakultas by remember { mutableStateOf("Pilih Fakultas") }
    var selectedJurusan by remember { mutableStateOf("Pilih Jurusan") }
    var availableJurusan by remember { mutableStateOf(listOf<String>()) }
    var email by remember { mutableStateOf("contoh@email.com") }
    var phone by remember { mutableStateOf("08123456789") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("krs.pdf") }

    val fakultasOptions = listOf("Pilih Fakultas") + fakultasList.map { it.nama }

    val isFormValid by derivedStateOf {
        name.isNotEmpty() &&
                nim.isNotEmpty() &&
                email.isNotEmpty() &&
                phone.isNotEmpty() &&
                selectedFakultas != "Pilih Fakultas" &&
                selectedJurusan != "Pilih Jurusan"
    }

    var showSuccessBanner by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = it.lastPathSegment?.substringAfterLast("/") ?: "File dipilih"
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showSuccessBanner) {
                    RegistrationSuccessBanner(
                        eventName = eventName,
                        successText = "Pendaftaran Event Diperbarui"
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
                    onValueChange = { nim = it }
                )
                DropdownInput(
                    label = "Fakultas",
                    selectedOption = selectedFakultas,
                    options = fakultasOptions,
                    onOptionSelected = { newFakultas ->
                        selectedFakultas = newFakultas
                        selectedJurusan = "Pilih Jurusan"
                        availableJurusan = fakultasList.find { it.nama == newFakultas }?.jurusan ?: emptyList()
                    }
                )
                DropdownInput(
                    label = "Jurusan",
                    selectedOption = selectedJurusan,
                    options = if (availableJurusan.isEmpty()) listOf("Pilih Jurusan") else availableJurusan,
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
                    label = "KRS",
                    fileName = selectedFileName,
                    onUploadClick = { filePickerLauncher.launch("application/pdf") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                PrimaryButton(
                    text = "Simpan Perubahan",
                    onClick = {
                        if (isFormValid) showSuccessBanner = true
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth(0.6f)
                )

                Spacer(modifier = Modifier.height(40.dp))
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
