package com.example.uventapp.ui.screen.registration

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.uventapp.data.model.fakultasList
import com.example.uventapp.ui.components.CustomAppBar
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.LightBackground

// ---------------------------
// Helper Composables
// ---------------------------

@Composable
fun FormInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
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

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
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
}

@Composable
fun UploadKRSInput(label: String, fileName: String, onUploadClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = fileName,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onUploadClick) { Text("Upload") }
        }
    }
}

// ---------------------------
// Main Registration Form
// ---------------------------

@SuppressLint("UnrememberedMutableState")
@Composable
fun RegistrationFormScreen(navController: NavController, eventName: String) {
    var name by remember { mutableStateOf("") }
    var nim by remember { mutableStateOf("") }
    var selectedFakultas by remember { mutableStateOf("Pilih Fakultas") }
    var selectedJurusan by remember { mutableStateOf("Pilih Jurusan") }
    var availableJurusan by remember { mutableStateOf(listOf<String>()) }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("Tidak ada file dipilih") }

    val fakultasOptions = listOf("Pilih Fakultas") + fakultasList.map { it.nama }

    val isFormValid by derivedStateOf {
        name.isNotEmpty() &&
                nim.isNotEmpty() &&
                email.isNotEmpty() &&
                phone.isNotEmpty() &&
                selectedFakultas != "Pilih Fakultas" &&
                selectedJurusan != "Pilih Jurusan" &&
                selectedFileUri != null
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FormInputField("Nama Lengkap", name) { name = it }
            FormInputField("NIM", nim) { nim = it }

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

            FormInputField("Email", email) { email = it }
            FormInputField("No Telepon", phone) { phone = it }

            UploadKRSInput(
                label = "KRS",
                fileName = selectedFileName,
                onUploadClick = { filePickerLauncher.launch("application/pdf") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = "Daftar",
                onClick = {
                    if (isFormValid) {
                        // --- INI BAGIAN YANG DIPERBAIKI ---
                        // Menggunakan createRoute untuk membuat rute dengan parameter opsional
                        val finalEventName = eventName.ifEmpty { "Event" }
                        navController.navigate(Screen.MyRegisteredEvent.createRoute(finalEventName)) {
                            // PopUp ke start destination agar tombol back kembali ke home
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            // Set start destination baru ke Home
                            navController.graph.setStartDestination(Screen.Home.route)
                            launchSingleTop = true
                        }
                        // ------------------------------------
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}