package com.example.uventapp.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // Kita POST data LoginRequest sebagai JSON (@Body)
    @POST("login") // "login" adalah nama Resource Anda di MockAPI
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>
}