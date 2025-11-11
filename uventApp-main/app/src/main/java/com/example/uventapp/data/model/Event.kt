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
    val thumbnailUri: String? = null
)

// Pindahkan dummy data ke sini agar bisa diakses secara global
val dummyEvents = listOf(
    Event(
        id = 1,
        title = "Business Talkshow",
        type = "Talkshow",
        date = "16/10/2025",
        timeStart = "09:00",
        timeEnd = "12:00",
        platformType = "Offline",
        locationDetail = "Auditorium Unand",
        quota = "500",
        status = "Aktif",
        thumbnailResId = R.drawable.event_talkshow,
        thumbnailUri = null
    ),
    Event(
        id = 2,
        title = "Seminar Nasional AI",
        type = "Seminar",
        date = "20/10/2025",
        timeStart = "13:00",
        timeEnd = "15:00",
        platformType = "Offline",
        locationDetail = "Convention Hall Unand",
        quota = "1000",
        status = "Aktif",
        thumbnailResId = R.drawable.event_seminar,
        thumbnailUri = null
    ),
    Event(
        id = 3,
        title = "UI/UX Skill Lab",
        type = "Skill Lab",
        date = "28/10/2025",
        timeStart = "10:00",
        timeEnd = "14:00",
        platformType = "Online",
        locationDetail = "Zoom Meeting",
        quota = "50",
        status = "Aktif",
        thumbnailResId = R.drawable.event_skill_lab,
        thumbnailUri = null
    )
)