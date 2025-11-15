package com.example.uventapp.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // --- PERBAIKAN 1: Path diubah ---
    // Path lama: "login"
    // Path baru: "auth/login" (sesuai routes/auth.js)
    @POST("auth/login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    // --- TAMBAHAN BARU: Endpoint untuk Register ---
    // Ini akan memanggil [POST] /api/auth/register
    @POST("auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>
}
