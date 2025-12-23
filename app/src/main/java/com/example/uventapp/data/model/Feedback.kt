package com.example.uventapp.data.model

data class Feedback(
    // ID unik untuk feedback itu sendiri
    val id: Int,
    val eventId: Int,
    val rating: Int,
    val review: String,
    val photoUri: String?,

    // Properti baru untuk UI "Semua Feedback"
    val userName: String,
    val postDate: String,
    // boolean untuk menandai apakah ini ulasan dari user saat ini
    val isAnda: Boolean = false
)