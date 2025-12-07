package com.example.uventapp.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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

    @GET("events/my-events/{userId}")
    fun getMyCreatedEvents(
        @Path("userId") userId: Int
    ): Call<GetEventsResponse>

    // --- API UPDATE EVENT ---
    @PUT("events/{id}")
    fun updateEvent(
        @Path("id") eventId: Int,
        @Body request: UpdateEventRequest
    ): Call<UpdateEventResponse>

    // --- API PENDAFTARAN EVENT ---
    @POST("registrations")
    fun registerForEvent(
        @Body request: EventRegistrationRequest
    ): Call<EventRegistrationResponse>

    // --- API FEEDBACK ---
    @POST("feedback")
    fun createFeedback(
        @Body request: FeedbackRequest
    ): Call<FeedbackResponse>

    @GET("feedback/event/{eventId}")
    fun getFeedbacksByEvent(
        @Path("eventId") eventId: Int
    ): Call<GetFeedbacksResponse>

    // --- API DOKUMENTASI ---
    @POST("documentations")
    fun createDocumentation(
        @Body request: DocumentationRequest
    ): Call<DocumentationResponse>

    @GET("documentations/event/{eventId}")
    fun getDocumentationsByEvent(
        @Path("eventId") eventId: Int
    ): Call<GetDocumentationsResponse>
}
