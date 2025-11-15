package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

// Data class ini menangkap respons dari API Register
// (JSON: { "status": "success", "message": "Registrasi berhasil" })
data class RegisterResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String
)
