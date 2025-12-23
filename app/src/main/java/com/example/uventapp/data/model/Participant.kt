package com.example.uventapp.data.model

// Model untuk data peserta event (untuk admin/organizer)
data class Participant(
    val registrationId: Int,
    val eventId: Int,
    val userId: Int?,
    val name: String,
    val nim: String,
    val fakultas: String,
    val jurusan: String,
    val email: String,
    val phone: String,
    val krsUri: String?,
    val createdAt: String,
    val userName: String?
)
