package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class FeedbackResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: FeedbackData?
)

data class FeedbackData(
    @SerializedName("feedback_id")
    val feedbackId: Int
)

// Response untuk mengambil list feedback
data class GetFeedbacksResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: List<FeedbackItem>
)

data class FeedbackItem(
    @SerializedName("id")
    val id: Int,

    @SerializedName("event_id")
    val eventId: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("review")
    val review: String?,

    @SerializedName("photo_uri")
    val photoUri: String?,

    @SerializedName("user_name")
    val userName: String?,

    @SerializedName("created_at")
    val createdAt: String?
)
