package com.swadratna.swadratna_admin.utils

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JwtUtils @Inject constructor() {

    /**
     * Extract expiry time from JWT token
     * @param token The JWT token (with or without "Bearer " prefix)
     * @return Expiry time in milliseconds, or null if parsing fails
     */
    fun getTokenExpiryTime(token: String?): Long? {
        if (token.isNullOrEmpty()) return null
        
        return try {
            // Remove "Bearer " prefix if present
            val cleanToken = token.removePrefix("Bearer ").trim()
            
            // JWT has 3 parts separated by dots: header.payload.signature
            val parts = cleanToken.split(".")
            if (parts.size != 3) return null
            
            // Decode the payload (second part)
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING)
            val decodedString = String(decodedBytes, StandardCharsets.UTF_8)
            
            // Parse JSON to get expiry
            val jsonObject = JSONObject(decodedString)
            
            // 'exp' field contains expiry time in seconds (Unix timestamp)
            if (jsonObject.has("exp")) {
                val expSeconds = jsonObject.getLong("exp")
                // Convert to milliseconds
                return expSeconds * 1000L
            }
            
            null
        } catch (e: Exception) {
            // Log error in debug mode
            println("Error parsing JWT token: ${e.message}")
            null
        }
    }
    
    /**
     * Check if token is expired
     * @param token The JWT token
     * @return true if token is expired, false otherwise
     */
    fun isTokenExpired(token: String?): Boolean {
        val expiryTime = getTokenExpiryTime(token) ?: return true
        return System.currentTimeMillis() >= expiryTime
    }
    
    /**
     * Get remaining time until token expires
     * @param token The JWT token
     * @return Remaining time in milliseconds, or 0 if expired/invalid
     */
    fun getRemainingTime(token: String?): Long {
        val expiryTime = getTokenExpiryTime(token) ?: return 0L
        val remaining = expiryTime - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0L
    }
    
    /**
     * Extract user ID from JWT token
     * @param token The JWT token
     * @return User ID or null if not found
     */
    fun getUserIdFromToken(token: String?): String? {
        if (token.isNullOrEmpty()) return null
        
        return try {
            val cleanToken = token.removePrefix("Bearer ").trim()
            val parts = cleanToken.split(".")
            if (parts.size != 3) return null
            
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING)
            val decodedString = String(decodedBytes, StandardCharsets.UTF_8)
            
            val jsonObject = JSONObject(decodedString)
            
            // Common fields for user ID: sub, user_id, userId, id
            when {
                jsonObject.has("sub") -> jsonObject.getString("sub")
                jsonObject.has("user_id") -> jsonObject.getString("user_id")
                jsonObject.has("userId") -> jsonObject.getString("userId")
                jsonObject.has("id") -> jsonObject.getString("id")
                else -> null
            }
        } catch (e: Exception) {
            println("Error extracting user ID from token: ${e.message}")
            null
        }
    }
}