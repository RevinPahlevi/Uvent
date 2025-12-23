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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
// --- Import yang Diperlukan ---
import com.example.uventapp.data.network.ApiClient
import com.example.uventapp.data.network.LoginRequest
import com.example.uventapp.data.network.LoginResponse
import com.example.uventapp.ui.components.AuthInputField
import com.example.uventapp.ui.components.PrimaryButton
import com.example.uventapp.ui.navigation.Screen
// --- Import ViewModel ---
import com.example.uventapp.ui.screen.profile.ProfileViewModel
import com.example.uventapp.ui.theme.LightBackground
import com.example.uventapp.ui.theme.PrimaryGreen
import com.example.uventapp.ui.theme.TextLink
import com.example.uventapp.ui.theme.White
// --- Import Pengecek Jaringan ---
import com.example.uventapp.utils.isNetworkAvailable
import com.example.uventapp.utils.FCMTokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LoginScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel // <-- Terima ViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // State untuk loading dan pesan error
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // --- Fungsi untuk memanggil API Login ---
    fun startLogin() {
        // 1. Cek koneksi internet
        if (!isNetworkAvailable(context)) {
            errorMessage = "Tidak ada koneksi internet."
            return
        }

        isLoading = true
        errorMessage = null

        // 2. Buat request body
        val request = LoginRequest(email = email, password = password)

        // 3. Panggil API (Logika bypass dihapus)
        ApiClient.instance.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                isLoading = false
                val body = response.body()

                // 4. Cek jika API sukses DAN status-nya "success"
                if (response.isSuccessful && body?.status == "success" && body.data != null) {

                    // --- PENTING: Simpan data user ke ViewModel dan SharedPreferences ---
                    profileViewModel.saveUserProfile(body.data.user, context)
                    // ----------------------------------------------
                    
                    // Save FCM token to backend for push notifications
                    FCMTokenManager.saveFCMTokenToBackend(context, body.data.user.id)

                    Log.d("LoginScreen", "Login API berhasil. User: ${body.data.user.name}")
                    // 5. Navigasi ke Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    // Jika gagal (data salah / server error)
                    val errorMsg = response.body()?.message ?: "Email atau password salah."
                    Log.e("LoginScreen", "API Error: ${response.code()} - $errorMsg")
                    errorMessage = errorMsg
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Jika server tidak terjangkau
                isLoading = false
                errorMessage = "Gagal terhubung ke server. Coba lagi nanti."
                Log.e("LoginScreen", "API Failure: ${t.message}")
            }
        })
    }
    // ------------------------------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LightBackground,
                        Color(0xFFE8F5E9) // Subtle green tint at bottom
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Masuk",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
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
                                startLogin() // Panggil fungsi API
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
