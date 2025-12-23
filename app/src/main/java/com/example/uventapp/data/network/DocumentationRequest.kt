package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class DocumentationRequest(
    @SerializedName("event_id")
    val eventId: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("description")
    val description: String?,

    @SerializedName("photo_uri")
    val photoUri: String?
)
