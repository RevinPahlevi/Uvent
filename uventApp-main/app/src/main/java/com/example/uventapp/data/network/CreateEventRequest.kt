package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class CreateEventRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("time_start")
    val timeStart: String,
    @SerializedName("time_end")
    val timeEnd: String,
    @SerializedName("platform_type")
    val platformType: String,
    @SerializedName("location_detail")
    val locationDetail: String,
    @SerializedName("quota")
    val quota: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("thumbnail_uri")
    val thumbnailUri: String?,

    @SerializedName("creator_id") // <-- TAMBAHAN BARU
    val creatorId: Int?
)
