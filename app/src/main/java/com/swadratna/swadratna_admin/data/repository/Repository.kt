package com.swadratna.swadratna_admin.data.repository

import javax.inject.Inject

interface Repository {
    // Define your repository methods here
    suspend fun getExample(): Result<String>
}

class RepositoryImpl @Inject constructor() : Repository {
    override suspend fun getExample(): Result<String> {
        return Result.success("Repository Pattern Setup Complete")
    }
}