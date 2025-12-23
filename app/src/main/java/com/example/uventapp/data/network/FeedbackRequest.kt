package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class FeedbackRequest(
    @SerializedName("event_id")
    val eventId: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("review")
    val review: String?,

    @SerializedName("photo_uri")
    val photoUri: String?
)
