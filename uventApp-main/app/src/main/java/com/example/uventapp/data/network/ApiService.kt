package com.example.uventapp.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path // <-- IMPORT BARU

interface ApiService {

    @POST("auth/login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    @POST("events")
    fun createEvent(
        @Body request: CreateEventRequest
    ): Call<CreateEventResponse>

    @GET("events")
    fun getAllEvents(): Call<GetEventsResponse>

    // --- FUNGSI API BARU ---
    // Memanggil [GET] /api/events/my-events/{userId}
    @GET("events/my-events/{userId}")
    fun getMyCreatedEvents(
        @Path("userId") userId: Int
    ): Call<GetEventsResponse> // Kita bisa pakai GetEventsResponse yang sama
}
