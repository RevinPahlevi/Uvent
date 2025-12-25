package com.example.uventapp.data.model

import com.example.uventapp.R
import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id")
    val id: Int,
    
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
    
    val thumbnailResId: Int? = null,
    
    @SerializedName("thumbnail_uri")
    val thumbnailUri: String? = null,
    
    @SerializedName("creator_id")
    val creatorId: Int? = null
)
