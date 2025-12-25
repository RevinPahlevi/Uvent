package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class DeleteResponse(
    val status: String,
    val message: String
)

data class UpdateFeedbackRequest(
    @SerializedName("user_id") val userId: Int,
    val rating: Int,
    val review: String?,
    @SerializedName("photo_uri") val photoUri: String?
)

data class UpdateFeedbackResponse(
    val status: String,
    val message: String
)

data class UpdateDocumentationRequest(
    val description: String
)

data class UpdateDocumentationResponse(
    val status: String,
    val message: String
)

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

data class RegistrationIdResponse(
    val status: String,
    val data: RegistrationIdData?
)

data class RegistrationIdData(
    @com.google.gson.annotations.SerializedName("registration_id")
    val registrationId: Int
)

data class RegistrationDataResponse(
    val status: String,
    val data: RegistrationDataFull?
)

data class UpdateRegistrationRequest(
    val name: String,
    val nim: String,
    val fakultas: String,
    val jurusan: String,
    val email: String,
    val phone: String,
    @SerializedName("krs_uri") val krsUri: String?
)

data class UpdateRegistrationResponse(
    val status: String,
    val message: String
)

data class NimCheckResponse(
    val status: String,
    val available: Boolean,
    val message: String
)
