package com.example.uventapp.data.model

// Model data baru, terpisah dari Feedback
data class Documentation(
    val id: Int,
    val eventId: Int,
    val description: String,
    val photoUri: String?,
    val userName: String,
    val postDate: String,
    val postTime: String,
    val isAnda: Boolean = false
)

