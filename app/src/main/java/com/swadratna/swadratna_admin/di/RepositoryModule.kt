package com.swadratna.swadratna_admin.di

import com.swadratna.swadratna_admin.data.repository.ActivityRepository
import com.swadratna.swadratna_admin.data.repository.ActivityRepositoryImpl
import com.swadratna.swadratna_admin.data.repository.AnalyticsRepository
import com.swadratna.swadratna_admin.data.repository.AnalyticsRepositoryImpl
import com.swadratna.swadratna_admin.data.repository.AuthRepository
import com.swadratna.swadratna_admin.data.repository.AuthRepositoryImpl
import com.swadratna.swadratna_admin.data.repository.AssetRepository
import com.swadratna.swadratna_admin.data.repository.AssetRepositoryImpl
import com.swadratna.swadratna_admin.data.repository.Repository
import com.swadratna.swadratna_admin.data.repository.RepositoryImpl
import com.swadratna.swadratna_admin.data.repository.StaffRepository
import com.swadratna.swadratna_admin.data.repository.StaffRepositoryImpl
import com.swadratna.swadratna_admin.data.repository.StoreRepository
import com.swadratna.swadratna_admin.data.repository.StoreRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindStoreRepository(impl: StoreRepositoryImpl): StoreRepository
    
    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository
    
    @Binds
    @Singleton
    abstract fun bindAssetRepository(impl: AssetRepositoryImpl): AssetRepository
    
    @Binds
    @Singleton
    abstract fun bindStaffRepository(impl: StaffRepositoryImpl): StaffRepository

    @Binds
    @Singleton
    abstract fun bindRestaurantRepository(impl: com.swadratna.swadratna_admin.data.repository.RestaurantRepositoryImpl): com.swadratna.swadratna_admin.data.repository.RestaurantRepository

    @Binds
    @Singleton
    abstract fun bindWithdrawalRepository(impl: com.swadratna.swadratna_admin.data.repository.WithdrawalRepositoryImpl): com.swadratna.swadratna_admin.data.repository.WithdrawalRepository
}