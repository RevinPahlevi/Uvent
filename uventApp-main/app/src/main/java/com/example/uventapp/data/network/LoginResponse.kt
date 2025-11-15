package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

/*
 File ini diubah total untuk mencocokkan respons JSON dari server Node.js:
 {
    "status": "success",
    "message": "Login berhasil",
    "data": {
        "token": "...",
        "user": {
            "id": 1,
            "name": "...",
            "email": "...",
            "phone": "..."
        }
    }
 }
*/

// 1. Data class utama (Top-level JSON)
data class LoginResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: UserData? // Objek 'data' yang bisa jadi null jika login gagal
)

// 2. Data class untuk objek "data"
data class UserData(
    @SerializedName("token")
    val token: String,

    @SerializedName("user")
    val user: User
)

// 3. Data class untuk objek "user"
data class User(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String
)
