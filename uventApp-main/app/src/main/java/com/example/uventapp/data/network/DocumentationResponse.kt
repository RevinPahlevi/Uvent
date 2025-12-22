package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class DocumentationResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: DocumentationData?
)

data class DocumentationData(
    @SerializedName("documentation_id")
    val documentationId: Int
)

// Response untuk mengambil list dokumentasi
data class GetDocumentationsResponse(
    @SerializedName("status")
    val status: String?,

    @SerializedName("data")
    val data: List<DocumentationItem>?  // Made nullable to prevent crash
)

data class DocumentationItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("event_id")
    val eventId: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("description")
    val description: String?,

    @SerializedName("photo_uri")
    val photoUri: String?,

    @SerializedName("user_name")
    val userName: String?,

    @SerializedName("created_at")
    val createdAt: String?
)
