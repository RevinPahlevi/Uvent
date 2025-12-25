package com.example.uventapp.ui.screen.profile

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.uventapp.data.network.User

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
    
    fun saveUserProfile(user: User, context: Context) {
        _profile.value = UserProfile(
            id = user.id,
            name = user.name,
            email = user.email,
            phone = user.phone
        )
        
        val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        prefs.edit().putInt("user_id", user.id).apply()
    }
    
    fun logout() {
        _profile.value = null
    }
    
    fun logout(context: Context) {
        _profile.value = null
        
        val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
