package com.swadratna.swadratna_admin.data.repository

import com.swadratna.swadratna_admin.data.remote.api.CustomersApi
import com.swadratna.swadratna_admin.data.remote.dto.CustomerListResponse
import com.swadratna.swadratna_admin.data.remote.dto.CustomerDto
import com.swadratna.swadratna_admin.utils.NetworkErrorHandler
import javax.inject.Inject

interface CustomersRepository {
    suspend fun list(page: Int?, limit: Int?, status: String?, search: String?): Result<CustomerListResponse>
    suspend fun block(id: String): Result<CustomerDto>
    suspend fun unblock(id: String): Result<CustomerDto>
    suspend fun delete(id: String): Result<Unit>
}

class CustomersRepositoryImpl @Inject constructor(
    private val api: CustomersApi
) : CustomersRepository {
    override suspend fun list(page: Int?, limit: Int?, status: String?, search: String?): Result<CustomerListResponse> {
        return try {
            Result.success(api.listCustomers(page, limit, status, search))
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    override suspend fun block(id: String): Result<CustomerDto> {
        return try {
            Result.success(api.blockCustomer(id))
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    override suspend fun unblock(id: String): Result<CustomerDto> {
        return try {
            Result.success(api.unblockCustomer(id))
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }

    override suspend fun delete(id: String): Result<Unit> {
        return try {
            api.deleteCustomer(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(NetworkErrorHandler.getErrorMessage(e), e))
        }
    }
}

