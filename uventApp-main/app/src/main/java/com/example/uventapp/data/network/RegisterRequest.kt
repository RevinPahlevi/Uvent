package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

// Data class ini membawa data dari RegistrationScreen ke API
data class RegisterRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("phone")
    val phone: String
)
