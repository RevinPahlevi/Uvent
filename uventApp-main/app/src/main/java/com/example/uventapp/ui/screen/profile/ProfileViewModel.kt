package com.example.uventapp.ui.screen.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.uventapp.data.network.User

// Data class untuk menampung info pengguna
data class UserProfile(
    val id: Int, // <-- TAMBAHAN BARU
    val name: String,
    val email: String,
    val phone: String
)

class ProfileViewModel : ViewModel() {

    private val _profile = mutableStateOf<UserProfile?>(null)
    val profile: State<UserProfile?> = _profile

    fun saveUserProfile(user: User) {
        _profile.value = UserProfile(
            id = user.id, // <-- TAMBAHAN BARU
            name = user.name,
            email = user.email,
            phone = user.phone
        )
    }
}
