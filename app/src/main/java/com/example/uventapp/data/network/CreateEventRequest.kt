package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class CreateEventRequest(
    @SerializedName("title")
    val title: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("timeStart")
    val timeStart: String,

    @SerializedName("timeEnd")
    val timeEnd: String,

    @SerializedName("platformType")
    val platformType: String,

    @SerializedName("locationDetail")
    val locationDetail: String,

    @SerializedName("quota")
    val quota: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("thumbnailUri")
    val thumbnailUri: String?,

    @SerializedName("creator_id")
    val creatorId: Int?
)

