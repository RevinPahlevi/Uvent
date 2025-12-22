package com.example.uventapp.utils

/**
 * Helper object to fix image URLs for Android
 * Extracts filename from any URL and rebuilds with correct server IP
 */
object ImageUrlHelper {
    
    // Base URL for the server - UPDATE THIS IF YOUR SERVER IP CHANGES
    // This should match the IP in ApiClient.kt (without /api/)
    private const val SERVER_BASE_URL = "http://192.168.1.19:3000"
    
    /**
     * Fixes image URLs by extracting filename and rebuilding with correct server IP
     * This handles cases where:
     * - IP address has changed since upload
     * - URL uses localhost/127.0.0.1
     * - URL is a relative path
     * 
     * @param url The original URL from the server/database
     * @return A fixed URL that can be accessed from Android device/emulator
     */
    fun fixImageUrl(url: String?): String? {
        if (url.isNullOrEmpty()) return null
        
        return try {
            when {
                // If URL contains /uploads/, extract the path and rebuild
                url.contains("/uploads/") -> {
                    val uploadsIndex = url.indexOf("/uploads/")
                    val path = url.substring(uploadsIndex)
                    "$SERVER_BASE_URL$path"
                }
                // If URL is just a filename (no path)
                url.matches(Regex("^[a-zA-Z0-9_-]+\\.(jpg|jpeg|png|gif|webp)$")) -> {
                    "$SERVER_BASE_URL/uploads/$url"
                }
                // If URL is a relative path starting with /
                url.startsWith("/") -> {
                    "$SERVER_BASE_URL$url"
                }
                // If URL already starts with http but might have wrong IP
                url.startsWith("http://") && url.contains(":3000") -> {
                    // Extract path after :3000
                    val portIndex = url.indexOf(":3000")
                    val path = url.substring(portIndex + 5) // +5 for ":3000"
                    "$SERVER_BASE_URL$path"
                }
                // Return as-is for https or other cases
                else -> url
            }
        } catch (e: Exception) {
            // If any error, return original URL
            url
        }
    }
}
