package com.swadratna.swadratna_admin.di

import com.swadratna.swadratna_admin.data.repository.Repository
import com.swadratna.swadratna_admin.data.repository.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindRepository(repositoryImpl: RepositoryImpl): Repository
}