package com.example.uventapp.utils

object ImageUrlHelper {
    
    private const val SERVER_BASE_URL = "http://192.168.100.87:3000"
    
    fun fixImageUrl(url: String?): String? {
        if (url.isNullOrEmpty()) return null
        
        return try {
            when {
                url.contains("/uploads/") -> {
                    val uploadsIndex = url.indexOf("/uploads/")
                    val path = url.substring(uploadsIndex)
                    "$SERVER_BASE_URL$path"
                }
                url.matches(Regex("^[a-zA-Z0-9_-]+\\.(jpg|jpeg|png|gif|webp)$")) -> {
                    "$SERVER_BASE_URL/uploads/$url"
                }
                url.startsWith("/") -> {
                    "$SERVER_BASE_URL$url"
                }
                url.startsWith("http://") && url.contains(":3000") -> {
                    val portIndex = url.indexOf(":3000")
                    val path = url.substring(portIndex + 5)
                    "$SERVER_BASE_URL$path"
                }
                else -> url
            }
        } catch (e: Exception) {
            url
        }
    }
}
