package com.example.uventapp.data.model

// Mengisi data class Registration yang sudah ada
data class Registration(
    val eventId: Int,
    val name: String,
    val nim: String,
    val fakultas: String,
    val jurusan: String,
    val email: String,
    val phone: String,
    val krsUri: String? // Simpan URI sebagai String
)