package com.example.uventapp.ui.screen.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
// --- Import data class User dari paket network ---
import com.example.uventapp.data.network.User

// Data class untuk menampung info pengguna di UI
data class UserProfile(
    val name: String,
    val email: String,
    val phone: String
)

class ProfileViewModel : ViewModel() {

    // State untuk menyimpan profil pengguna
    private val _profile = mutableStateOf<UserProfile?>(null)
    val profile: State<UserProfile?> = _profile

    // --- HAPUS BLOK init ---
    // Kita tidak ingin memuat data dummy lagi
    // init {
    //     loadUserProfile()
    // }

    // --- HAPUS FUNGSI loadUserProfile() ---
    // private fun loadUserProfile() { ... }

    // --- FUNGSI BARU ---
    /**
     * Dipanggil oleh LoginScreen untuk menyimpan data pengguna
     * setelah login API berhasil.
     */
    fun saveUserProfile(user: User) {
        // Kita petakan data dari 'User' (Network) ke 'UserProfile' (UI)
        _profile.value = UserProfile(
            name = user.name,
            email = user.email,
            phone = user.phone
        )
    }
}
