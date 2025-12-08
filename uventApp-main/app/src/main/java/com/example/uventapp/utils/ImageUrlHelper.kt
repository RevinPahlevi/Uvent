package com.example.uventapp.utils

/**
 * Helper object to fix image URLs for Android
 * Replaces localhost with the actual server IP so images can be loaded from the server
 */
object ImageUrlHelper {
    
    // Base URL for the server (same as in ApiClient)
    // Update this if your server IP changes
    private const val SERVER_BASE_URL = "http://192.168.1.62:3000"
    
    /**
     * Fixes image URLs that might be using localhost or relative paths
     * @param url The original URL from the server
     * @return A fixed URL that can be accessed from Android device/emulator
     */
    fun fixImageUrl(url: String?): String? {
        if (url == null || url.isEmpty()) return null
        
        return when {
            // If URL starts with localhost, replace it with server IP
            url.startsWith("http://localhost:3000") -> {
                url.replace("http://localhost:3000", SERVER_BASE_URL)
            }
            url.startsWith("http://127.0.0.1:3000") -> {
                url.replace("http://127.0.0.1:3000", SERVER_BASE_URL)
            }
            // If URL is a relative path starting with /uploads
            url.startsWith("/uploads") -> {
                "$SERVER_BASE_URL$url"
            }
            // If URL already has the correct server IP or is a full URL
            else -> url
        }
    }
}
