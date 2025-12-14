package com.example.uventapp.ui.screen.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
// --- Import yang Diperlukan ---
import com.example.uventapp.data.network.ApiClient
import com.example.uventapp.data.network.RegisterRequest
import com.example.uventapp.data.network.RegisterResponse
import com.example.uventapp.ui.components.AuthInputField
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.navigation.Screen
// --- Import ViewModel ---
import com.example.uventapp.ui.screen.profile.ProfileViewModel
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.TextLink
import com.example.uventapp.ui.theme.White
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun RegistrationScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel // <-- Terima ViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // --- State untuk loading dan pesan error ---
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- Fungsi untuk memanggil API Registrasi ---
    fun startRegistration() {
        isLoading = true
        errorMessage = null

        // 1. Buat request body
        val request = RegisterRequest(
            name = name,
            email = email,
            password = password,
            phone = phone
        )

        // 2. Panggil API
        ApiClient.instance.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                isLoading = false
                val body = response.body()

                if (response.isSuccessful && body?.status == "success") {
                    // Jika sukses, kembali ke Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    // Jika gagal (misal: email duplikat)
                    errorMessage = body?.message ?: "Registrasi gagal. Coba lagi."
                    Log.e("RegisterScreen", "API Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                // Jika server tidak terjangkau
                isLoading = false
                errorMessage = "Gagal terhubung ke server."
                Log.e("RegisterScreen", "API Failure: ${t.message}")
            }
        })
    }
    // ------------------------------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Daftar",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AuthInputField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nama",
                        icon = Icons.Filled.Person,
                        keyboardType = KeyboardType.Text
                    )
                    AuthInputField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        icon = Icons.Filled.Email,
                        keyboardType = KeyboardType.Email
                    )
                    AuthInputField(
                        value = phone,
                        onValueChange = { newValue ->
                            // Hanya terima angka atau string kosong (untuk delete)
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                phone = newValue
                            }
                        },
                        label = "No telepon",
                        icon = Icons.Filled.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                    AuthInputField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        icon = Icons.Filled.Lock,
                        keyboardType = KeyboardType.Password,
                        isPassword = true
                    )

                    // --- Tampilkan pesan error jika ada ---
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    PrimaryButton(
                        text = if (isLoading) "MENDAFTAR..." else "DAFTAR",
                        onClick = {
                            if (!isLoading) {
                                startRegistration() // Panggil fungsi API
                            }
                        },
                        enabled = !isLoading // Nonaktifkan tombol saat loading
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sudah memiliki akun? ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Text(
                    text = "Masuk",
                    color = TextLink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        // Jika pengguna klik "Masuk", navigasi ke Login
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
