package com.example.uventapp.ui.screen.profile

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
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
    
    /**
     * Save user profile and also persist user ID to SharedPreferences for background worker
     */
    fun saveUserProfile(user: User, context: Context) {
        _profile.value = UserProfile(
            id = user.id,
            name = user.name,
            email = user.email,
            phone = user.phone
        )
        
        // Save to SharedPreferences for background worker
        val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        prefs.edit().putInt("user_id", user.id).apply()
    }
    
    // Fungsi logout untuk menghapus data profil
    fun logout() {
        _profile.value = null
    }
    
    /**
     * Logout and clear SharedPreferences
     */
    fun logout(context: Context) {
        _profile.value = null
        
        // Clear SharedPreferences
        val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
