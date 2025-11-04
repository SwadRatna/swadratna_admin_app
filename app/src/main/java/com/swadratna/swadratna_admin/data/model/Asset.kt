package com.swadratna.swadratna_admin.data.model

// Domain model for Asset with camelCase fields

data class Asset(
    val cdnUrl: String?,
    val context: String?,
    val createdAt: String?,
    val expiresAt: String?,
    val fileName: String?,
    val fileSize: Long?,
    val id: Long?,
    val metadata: Map<String, Any?>?,
    val mimeType: String?,
    val status: String?,
    val storagePath: String?,
    val tenantId: Long?,
    val type: String?,
    val updatedAt: String?,
    val uploadedBy: Long?,
    val url: String?
)