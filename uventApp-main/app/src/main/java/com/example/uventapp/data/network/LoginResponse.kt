package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    // Nama variabel ini harus sama dengan skema di MockAPI
    @SerializedName("status")
    val status: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("token")
    val token: String
)