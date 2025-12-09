package com.example.uventapp.data.model

import com.example.uventapp.R

data class Event(
    val id: Int,
    val title: String,
    val type: String,
    val date: String, // Format: "d/M/yyyy" (contoh: "16/10/2025")
    val timeStart: String, // Format: "HH:mm" (contoh: "09:00")
    val timeEnd: String,   // Format: "HH:mm" (contoh: "12:00")
    val platformType: String, // "Online" atau "Offline"
    val locationDetail: String, // Link Zoom or "Auditorium"
    val quota: String,
    val status: String, // Ini akan jadi status default (misal "Aktif")
    val thumbnailResId: Int?,
    val thumbnailUri: String? = null,
    val creatorId: Int? = null // <-- TAMBAHAN BARU
)
