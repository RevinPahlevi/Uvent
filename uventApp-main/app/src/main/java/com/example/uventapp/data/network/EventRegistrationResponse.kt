package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class EventRegistrationResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: RegistrationData?
)

data class RegistrationData(
    @SerializedName("registration_id")
    val registrationId: Int
)
