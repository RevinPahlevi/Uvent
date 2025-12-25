package com.example.uventapp.data.model

data class Feedback(
    val id: Int,
    val eventId: Int,
    val rating: Int,
    val review: String,
    val photoUri: String?,
    val userName: String,
    val postDate: String,
    val isAnda: Boolean = false
)