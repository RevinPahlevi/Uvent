package com.example.uventapp.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.example.uventapp.data.model.GetNotificationsResponse
import com.example.uventapp.data.model.MarkAsReadResponse
import com.example.uventapp.data.model.SaveFCMTokenRequest
import com.example.uventapp.data.model.SaveFCMTokenResponse

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

    @PUT("events/{id}")
    fun updateEvent(
        @Path("id") eventId: Int,
        @Body request: UpdateEventRequest
    ): Call<UpdateEventResponse>

    @DELETE("events/{id}")
    fun deleteEvent(
        @Path("id") eventId: Int
    ): Call<DeleteResponse>

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

    @GET("registrations/user/{userId}")
    fun getMyRegistrationsByUserId(
        @Path("userId") userId: Int
    ): Call<GetEventsResponse>

    @GET("registrations/user/{userId}")
    fun getRegisteredEvents(
        @Path("userId") userId: Int
    ): Call<GetRegistrationsResponse>

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

    @DELETE("feedback/{id}/{userId}")
    fun deleteFeedback(
        @Path("id") feedbackId: Int,
        @Path("userId") userId: Int
    ): Call<DeleteResponse>

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

    @retrofit2.http.Multipart
    @POST("upload")
    fun uploadImage(
        @retrofit2.http.Part image: okhttp3.MultipartBody.Part
    ): Call<UploadImageResponse>
    
    @retrofit2.http.Multipart
    @POST("registrations/upload-krs")
    fun uploadKRS(
        @retrofit2.http.Part krs: okhttp3.MultipartBody.Part
    ): Call<UploadKRSResponse>
    
    @GET("registrations/event/{eventId}/participants")
    fun getParticipantsByEvent(
        @Path("eventId") eventId: Int
    ): Call<GetParticipantsResponse>

    @GET("registrations/check-nim/{eventId}/{nim}")
    fun checkNimExists(
        @Path("eventId") eventId: Int,
        @Path("nim") nim: String
    ): Call<CheckNimResponse>
    
    @GET("registrations/event/{eventId}/count")
    suspend fun getRegistrationCount(
        @Path("eventId") eventId: Int
    ): retrofit2.Response<RegistrationCountResponse>
    
    @GET("notifications/user/{userId}")
    fun getUserNotifications(
        @Path("userId") userId: Int
    ): Call<GetNotificationsResponse>
    
    @PUT("notifications/{id}/read")
    fun markNotificationAsRead(
        @Path("id") notificationId: Int
    ): Call<MarkAsReadResponse>
    
    @PUT("notifications/user/{userId}/read-all")
    fun markAllNotificationsAsRead(
        @Path("userId") userId: Int
    ): Call<MarkAsReadResponse>
    
    @POST("notifications/fcm-token")
    fun saveFCMToken(
        @Body request: SaveFCMTokenRequest
    ): Call<SaveFCMTokenResponse>
}

data class CheckNimResponse(
    val status: String,
    val data: CheckNimData?
)

data class CheckNimData(
    val exists: Boolean
)

data class RegistrationCountResponse(
    val status: String,
    val count: Int
)

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
