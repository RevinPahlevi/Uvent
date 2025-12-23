package com.example.uventapp.data.network

import com.google.gson.annotations.SerializedName

data class GetEventsResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: List<EventResponse>
)

data class EventResponse(
    @SerializedName(value = "id", alternate = ["event_id"])
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("type")
    val type: String?,

    @SerializedName("date")
    val date: String?,

    @SerializedName("time_start")
    val timeStart: String?,

    @SerializedName("time_end")
    val timeEnd: String?,

    @SerializedName("platform_type")
    val platformType: String?, // Kunci ini harus sama dengan output JSON dari backend

    @SerializedName("location_detail")
    val locationDetail: String?, // Kunci ini harus sama dengan output JSON dari backend

    @SerializedName("quota")
    val quota: Int?, // Backend mungkin mengirim angka (Int)

    @SerializedName("status")
    val status: String?,

    @SerializedName("thumbnail_uri")
    val thumbnailUri: String?,

    @SerializedName("creator_id")
    val creatorId: Int?
)
