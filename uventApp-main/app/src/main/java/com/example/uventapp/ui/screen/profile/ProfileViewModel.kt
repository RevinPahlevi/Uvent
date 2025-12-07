package com.example.uventapp.ui.screen.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.uventapp.data.network.User

// Data class untuk menampung info pengguna
data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String
)

class ProfileViewModel : ViewModel() {

    private val _profile = mutableStateOf<UserProfile?>(null)
    val profile: State<UserProfile?> = _profile

    fun saveUserProfile(user: User) {
        _profile.value = UserProfile(
            id = user.id,
            name = user.name,
            email = user.email,
            phone = user.phone
        )
    }
    
    // Fungsi logout untuk menghapus data profil
    fun logout() {
        _profile.value = null
    }
}
