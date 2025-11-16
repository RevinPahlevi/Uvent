package com.example.uventapp.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET // <-- IMPORT BARU
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    // --- TAMBAHAN BARU (MEMBUAT EVENT) ---
    @POST("events")
    fun createEvent(
        @Body request: CreateEventRequest
    ): Call<CreateEventResponse>

    // --- TAMBAHAN BARU (MENGAMBIL EVENT) ---
    @GET("events")
    fun getAllEvents(): Call<GetEventsResponse>
}
