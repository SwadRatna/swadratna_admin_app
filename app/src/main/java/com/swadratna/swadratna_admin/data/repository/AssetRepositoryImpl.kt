package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.model.Asset
import com.swadratna.swadratna_admin.data.remote.toDomain
import com.swadratna.swadratna_admin.data.remote.api.AssetApi
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

import com.swadratna.swadratna_admin.utils.NetworkErrorHandler

class AssetRepositoryImpl @Inject constructor(
    private val api: AssetApi
) : AssetRepository {

    override suspend fun uploadAsset(
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
        context: String,
        type: String
    ): Result<Asset> = withContext(Dispatchers.IO) {
        try {
            val mediaType = mimeType.toMediaTypeOrNull()
            val requestBody = bytes.toRequestBody(mediaType)
            val filePart = MultipartBody.Part.createFormData(
                name = "file",
                filename = fileName,
                body = requestBody
            )
            val contextPart: RequestBody = context.toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart: RequestBody = type.toRequestBody("text/plain".toMediaTypeOrNull())
            Result.success(api.uploadAsset(filePart, contextPart, typePart).toDomain())
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    override suspend fun getAsset(id: Long): Result<Asset> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.getAsset(id).toDomain())
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    override suspend fun deleteAsset(id: Long): Result<Asset> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.deleteAsset(id).toDomain())
        } catch (e: Throwable) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    // Remove confirm implementation as API not required now
    // override suspend fun confirmAsset(id: Long): Result<Asset> = withContext(Dispatchers.IO) {
    //     runCatching { api.confirmAsset(id).toDomain() }
    // }
}