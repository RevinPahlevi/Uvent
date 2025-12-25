package com.example.uventapp.data.model

data class Registration(
    val eventId: Int,
    val name: String,
    val nim: String,
    val fakultas: String,
    val jurusan: String,
    val email: String,
    val phone: String,
    val krsUri: String?,
    val registrationId: Int? = null
)