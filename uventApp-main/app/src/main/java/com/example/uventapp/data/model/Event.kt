package com.example.uventapp.data.model

import com.example.uventapp.R

/**
 * Data class ini sekarang menjadi SATU-SATUNYA definisi Event di seluruh aplikasi.
 */
data class Event(
    val id: Int, // Gunakan Int untuk ID agar konsisten
    val title: String,
    val type: String,
    val date: String,
    val time: String,
    val location: String,
    val quota: String, // Ubah ke String agar konsisten
    val status: String,
    // --- PERBAIKAN UTAMA DI SINI ---
    val thumbnailResId: Int?, // Dibuat nullable
    val thumbnailUri: String? = null // Ditambahkan untuk menampung URI poster
    // ---------------------------------
)

// Pindahkan dummy data ke sini agar bisa diakses secara global
val dummyEvents = listOf(
    Event(
        id = 1,
        title = "Business Talkshow",
        type = "Talkshow",
        date = "16 Okt 2025",
        time = "09:00 - 12:00",
        location = "Auditorium Unand",
        quota = "500",
        status = "Aktif",
        thumbnailResId = R.drawable.event_talkshow, // Data lama tetap pakai ResId
        thumbnailUri = null // (default)
    ),
    Event(
        id = 2,
        title = "Seminar Nasional AI",
        type = "Seminar",
        date = "20 Okt 2025",
        time = "13:00 - 15:00",
        location = "Convention Hall Unand",
        quota = "1000",
        status = "Aktif",
        thumbnailResId = R.drawable.event_seminar,
        thumbnailUri = null
    ),
    Event(
        id = 3,
        title = "UI/UX Skill Lab",
        type = "Skill Lab",
        date = "28 Okt 2025",
        time = "10:00 - 14:00",
        location = "Lab Komputer FTI",
        quota = "50",
        status = "Aktif",
        thumbnailResId = R.drawable.event_skill_lab,
        thumbnailUri = null
    )
)