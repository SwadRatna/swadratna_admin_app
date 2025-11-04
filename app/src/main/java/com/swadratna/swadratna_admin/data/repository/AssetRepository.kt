package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.Asset

interface AssetRepository {
    suspend fun uploadAsset(
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
        context: String = "store",
        type: String = "image"
    ): Result<Asset>

    suspend fun getAsset(id: Long): Result<Asset>

    suspend fun deleteAsset(id: Long): Result<Asset>

    // confirm no longer used
    // suspend fun confirmAsset(id: Long): Result<Asset>
}