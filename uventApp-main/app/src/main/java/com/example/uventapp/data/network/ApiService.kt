package com.example.uventapp.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    // --- API DELETE EVENT ---
    @DELETE("events/{id}")
    fun deleteEvent(
        @Path("id") eventId: Int
    ): Call<DeleteResponse>

    // --- API PENDAFTARAN EVENT ---
    @POST("registrations")
    fun registerForEvent(
        @Body request: EventRegistrationRequest
    ): Call<EventRegistrationResponse>

    @GET("registrations/user/{userId}")
    fun getMyRegistrations(
        @Path("userId") userId: Int
    ): Call<GetRegistrationsResponse>

    @PUT("registrations/{id}")
    fun updateRegistration(
        @Path("id") registrationId: Int,
        @Body request: UpdateRegistrationRequest
    ): Call<UpdateRegistrationResponse>

    @DELETE("registrations/{id}")
    fun cancelRegistration(
        @Path("id") registrationId: Int
    ): Call<DeleteResponse>

    // Get events yang diikuti user (by userId)
    @GET("registrations/user/{userId}")
    fun getMyRegistrationsByUserId(
        @Path("userId") userId: Int
    ): Call<GetEventsResponse>

    // --- API FEEDBACK ---
    @POST("feedback")
    fun createFeedback(
        @Body request: FeedbackRequest
    ): Call<FeedbackResponse>

    @GET("feedback/event/{eventId}")
    fun getFeedbacksByEvent(
        @Path("eventId") eventId: Int
    ): Call<GetFeedbacksResponse>

    @PUT("feedback/{id}")
    fun updateFeedback(
        @Path("id") feedbackId: Int,
        @Body request: UpdateFeedbackRequest
    ): Call<UpdateFeedbackResponse>

    @DELETE("feedback/{id}")
    fun deleteFeedback(
        @Path("id") feedbackId: Int
    ): Call<DeleteResponse>

    // --- API DOKUMENTASI ---
    @POST("documentations")
    fun createDocumentation(
        @Body request: DocumentationRequest
    ): Call<DocumentationResponse>

    @GET("documentations/event/{eventId}")
    fun getDocumentationsByEvent(
        @Path("eventId") eventId: Int
    ): Call<GetDocumentationsResponse>

    @PUT("documentations/{id}")
    fun updateDocumentation(
        @Path("id") documentationId: Int,
        @Body request: UpdateDocumentationRequest
    ): Call<UpdateDocumentationResponse>

    @DELETE("documentations/{id}")
    fun deleteDocumentation(
        @Path("id") documentationId: Int
    ): Call<DeleteResponse>

    // --- API UPLOAD GAMBAR ---
    @retrofit2.http.Multipart
    @POST("upload")
    fun uploadImage(
        @retrofit2.http.Part image: okhttp3.MultipartBody.Part
    ): Call<UploadImageResponse>

    // ===== FITUR BARU: Upload KRS & Lihat Peserta =====

    // Upload file KRS (PDF)
    @retrofit2.http.Multipart
    @POST("registrations/upload-krs")
    fun uploadKRS(
        @retrofit2.http.Part krs: okhttp3.MultipartBody.Part
    ): Call<UploadKRSResponse>

    // Get daftar peserta per event (untuk admin/creator)
    @GET("registrations/event/{eventId}/participants")
    fun getParticipantsByEvent(
        @Path("eventId") eventId: Int
    ): Call<GetParticipantsResponse>

    // ==================================================
}

// Response untuk upload KRS
data class UploadKRSResponse(
    val status: String,
    val message: String,
    val data: UploadKRSData?
)

data class UploadKRSData(
    val filename: String,
    val url: String,
    val size: Long
)

// Response untuk get participants
data class GetParticipantsResponse(
    val status: String,
    val data: List<ParticipantData>
)

data class ParticipantData(
    val registration_id: Int,
    val event_id: Int,
    val user_id: Int?,
    val name: String,
    val nim: String,
    val fakultas: String,
    val jurusan: String,
    val email: String,
    val phone: String,
    val krs_uri: String?,
    val created_at: String,
    val user_name: String?
)