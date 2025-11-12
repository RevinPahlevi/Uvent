package com.example.uventapp.ui.screen.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
import com.example.uventapp.data.network.ApiClient
import com.example.uventapp.data.network.LoginRequest
import com.example.uventapp.data.network.LoginResponse
import com.example.uventapp.ui.components.AuthInputField
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.navigation.Screen
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.TextLink
import com.example.uventapp.ui.theme.White
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
// --- PERBAIKAN: TAMBAHKAN IMPORT YANG HILANG ---
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
// ---------------------------------------------

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // State untuk loading dan pesan error
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fungsi untuk memanggil API
    fun startLogin() {
        isLoading = true
        errorMessage = null

        // --- PERBAIKAN: LOGIN DEFAULT (HARDCODED) ---
        // Email & Password ini sekarang dijadikan "kunci"
        val defaultEmail = "aldi@gmail.com"
        val defaultPassword = "aldi123"

        // Hapus delay jika ada, atau tambahkan delay singkat untuk simulasi
        kotlinx.coroutines.MainScope().launch {
            delay(500) // Simulasi loading 0.5 detik

            // Logika bypass: Cek apakah input cocok dengan data default
            if (email == defaultEmail && password == defaultPassword) {
                Log.d("LoginBypass", "Login default berhasil.")
                // Navigasi ke Home
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            } else {
                // Jika tidak cocok, tampilkan error
                Log.d("LoginBypass", "Email atau password salah.")
                errorMessage = "Email atau password salah."
            }
            isLoading = false // Selesaikan loading
        }
        // ----------------------------------------
    }

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
                text = "Masuk",
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
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        icon = Icons.Filled.Email,
                        keyboardType = KeyboardType.Email
                    )

                    AuthInputField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        icon = Icons.Filled.Lock,
                        keyboardType = KeyboardType.Password,
                        isPassword = true
                    )

                    // Tampilkan pesan error jika ada
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
                        text = if (isLoading) "LOADING..." else "MASUK",
                        onClick = {
                            if (!isLoading) {
                                startLogin() // Panggil fungsi (yang sudah dimodifikasi)
                            }
                        },
                        enabled = !isLoading // Nonaktifkan tombol saat loading
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Belum punya akun? ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Text(
                    text = "Daftar",
                    color = TextLink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }
        }
    }
}