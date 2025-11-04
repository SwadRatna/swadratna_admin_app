package com.swadratna.swadratna_admin.data.remote

import com.google.gson.annotations.SerializedName
import com.swadratna.swadratna_admin.data.model.Asset

// DTO matching server snake_case fields for assets

data class AssetDto(
    @SerializedName("cdn_url") val cdnUrl: String?,
    @SerializedName("context") val context: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName("file_name") val fileName: String?,
    @SerializedName("file_size") val fileSize: Long?,
    @SerializedName("id") val id: Long?,
    @SerializedName("metadata") val metadata: Map<String, Any?>?,
    @SerializedName("mime_type") val mimeType: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("storage_path") val storagePath: String?,
    @SerializedName("tenant_id") val tenantId: Long?,
    @SerializedName("type") val type: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("uploaded_by") val uploadedBy: Long?,
    @SerializedName("url") val url: String?
)

fun AssetDto.toDomain() = Asset(
    cdnUrl = cdnUrl,
    context = context,
    createdAt = createdAt,
    expiresAt = expiresAt,
    fileName = fileName,
    fileSize = fileSize,
    id = id,
    metadata = metadata,
    mimeType = mimeType,
    status = status,
    storagePath = storagePath,
    tenantId = tenantId,
    type = type,
    updatedAt = updatedAt,
    uploadedBy = uploadedBy,
    url = url
)