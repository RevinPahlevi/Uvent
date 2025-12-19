package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class EventRegistrationRequest(
    @SerializedName("event_id")
    val eventId: Int,

    @SerializedName("user_id")
    val userId: Int?,

    @SerializedName("name")
    val name: String,

    @SerializedName("nim")
    val nim: String,

    @SerializedName("fakultas")
    val fakultas: String,

    @SerializedName("jurusan")
    val jurusan: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("krs_uri")
    val krsUri: String?
)
