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
    val date: String, // Format: "d/M/yyyy" (contoh: "16/10/2025")
    
    @SerializedName("time_start")
    val timeStart: String, // Format: "HH:mm" (contoh: "09:00")
    
    @SerializedName("time_end")
    val timeEnd: String,   // Format: "HH:mm" (contoh: "12:00")
    
    @SerializedName("platform_type")
    val platformType: String, // "Online" atau "Offline"
    
    @SerializedName("location_detail")
    val locationDetail: String, // Link Zoom or "Auditorium"
    
    @SerializedName("quota")
    val quota: String,
    
    @SerializedName("status")
    val status: String, // Ini akan jadi status default (misal "Aktif")
    
    val thumbnailResId: Int? = null, // Ini hanya untuk gambar lokal (drawable resource)
    
    @SerializedName("thumbnail_uri")
    val thumbnailUri: String? = null, // Ini untuk gambar dari server
    
    @SerializedName("creator_id")
    val creatorId: Int? = null
)


