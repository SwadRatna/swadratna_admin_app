package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.remote.AssetDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface AssetApi {
    @Multipart
    @POST("api/v1/admin/assets/upload")
    suspend fun uploadAsset(
        @Part file: MultipartBody.Part,
        @Part("context") context: RequestBody?,
        @Part("type") type: RequestBody?
    ): AssetDto

    @GET("api/v1/admin/assets/{id}")
    suspend fun getAsset(
        @Path("id") id: Long
    ): AssetDto

    @DELETE("api/v1/admin/assets/{id}")
    suspend fun deleteAsset(
        @Path("id") id: Long
    ): AssetDto

}