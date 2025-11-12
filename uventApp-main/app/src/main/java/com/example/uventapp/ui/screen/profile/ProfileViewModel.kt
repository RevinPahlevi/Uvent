package com.example.uventapp.ui.screen.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

// Data class untuk menampung info pengguna
data class UserProfile(
    val name: String,
    val email: String,
    val phone: String
)

class ProfileViewModel : ViewModel() {

    // State untuk menyimpan profil pengguna
    private val _profile = mutableStateOf<UserProfile?>(null)
    val profile: State<UserProfile?> = _profile

    init {
        // Panggil fungsi untuk memuat data
        loadUserProfile()
    }

    private fun loadUserProfile() {
        // --- SIMULASI ---
        // Nanti, di sinilah tempat Anda memanggil API untuk mendapatkan
        // data pengguna setelah mereka login.

        // Untuk saat ini, kita gunakan data hardcoded dari screenshot Anda.
        _profile.value = UserProfile(
            name = "LOLY AMELIA NURZA",
            email = "2311521016_loly@student.unand.ac.id",
            phone = "082268251708"
        )
    }
}
