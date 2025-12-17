package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.data.remote.dto.CustomerListResponse
import com.swadratna.swadratna_admin.data.remote.dto.CustomerDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CustomersApi {
    @GET("api/v1/admin/customers")
    suspend fun listCustomers(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null
    ): CustomerListResponse

    @POST("api/v1/admin/customers/{id}/block")
    suspend fun blockCustomer(@Path("id") id: String): CustomerDto

    @POST("api/v1/admin/customers/{id}/unblock")
    suspend fun unblockCustomer(@Path("id") id: String): CustomerDto

    @DELETE("api/v1/admin/customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: String)
}

