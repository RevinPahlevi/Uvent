package com.example.uventapp.data.model

data class Documentation(
    val id: Int,
    val eventId: Int,
    val userId: Int = 0,
    val description: String,
    val photoUri: String?,
    val userName: String,
    val postDate: String,
    val postTime: String,
    val isAnda: Boolean = false
)
