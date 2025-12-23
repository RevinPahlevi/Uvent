package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class CreateEventResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String
)
