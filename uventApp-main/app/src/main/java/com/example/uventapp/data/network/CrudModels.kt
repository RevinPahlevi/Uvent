package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

// Generic delete response
data class DeleteResponse(
    val status: String,
    val message: String
)

// Update Feedback Request
data class UpdateFeedbackRequest(
    @SerializedName("user_id") val userId: Int,
    val rating: Int,
    val review: String?,
    @SerializedName("photo_uri") val photoUri: String?
)

// Update Feedback Response
data class UpdateFeedbackResponse(
    val status: String,
    val message: String
)

// Update Documentation Request
data class UpdateDocumentationRequest(
    @SerializedName("user_id") val userId: Int,
    val description: String?,
    @SerializedName("photo_uri") val photoUri: String?
)

// Update Documentation Response
data class UpdateDocumentationResponse(
    val status: String,
    val message: String
)

// Update Registration Request
data class UpdateRegistrationRequest(
    @SerializedName("user_id") val userId: Int?,
    val name: String,
    val nim: String,
    val fakultas: String,
    val jurusan: String,
    val email: String,
    val phone: String,
    @SerializedName("krs_uri") val krsUri: String?
)

// Update Registration Response
data class UpdateRegistrationResponse(
    val status: String,
    val message: String
)

// Get Registrations Response
data class GetRegistrationsResponse(
    val status: String,
    val data: List<RegistrationDataFull>?
)

data class RegistrationDataFull(
    @SerializedName("registration_id") val registrationId: Int,
    @SerializedName("event_id") val eventId: Int,
    val name: String,
    val nim: String,
    val fakultas: String,
    val jurusan: String,
    val email: String,
    val phone: String,
    @SerializedName("krs_uri") val krsUri: String?,
    @SerializedName("created_at") val createdAt: String,
    // Event data from JOIN
    val title: String?,
    val type: String?,
    val date: String?,
    @SerializedName("time_start") val timeStart: String?,
    @SerializedName("time_end") val timeEnd: String?,
    @SerializedName("platform_type") val platformType: String?,
    @SerializedName("location_detail") val locationDetail: String?,
    val quota: Int?,
    val status: String?,
    @SerializedName("thumbnail_uri") val thumbnailUri: String?,
    @SerializedName("creator_id") val creatorId: Int?
)

// Upload Image Response
data class UploadImageResponse(
    val status: String,
    val message: String,
    val data: UploadImageData?
)

data class UploadImageData(
    val filename: String,
    val url: String,
    val size: Long
)
