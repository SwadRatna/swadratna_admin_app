package com.swadratna.swadratna_admin.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.swadratna.swadratna_admin.data.LocalDateAdapter
import com.swadratna.swadratna_admin.data.remote.api.AnalyticsApi
import com.swadratna.swadratna_admin.data.remote.api.AssetApi
import com.swadratna.swadratna_admin.data.remote.api.AuthApiService
import com.swadratna.swadratna_admin.data.remote.api.AuthInterceptor
import com.swadratna.swadratna_admin.data.remote.api.CampaignApi
import com.swadratna.swadratna_admin.data.remote.api.DashboardApi
import com.swadratna.swadratna_admin.data.remote.api.MenuApi
import com.swadratna.swadratna_admin.data.remote.api.InventoryApi
import com.swadratna.swadratna_admin.data.remote.api.CustomersApi
import com.swadratna.swadratna_admin.data.remote.api.HeaderInterceptor
import com.swadratna.swadratna_admin.data.remote.api.StaffApiService
import com.swadratna.swadratna_admin.data.remote.api.StoreApiService
import com.swadratna.swadratna_admin.data.remote.api.TokenAuthenticator
import com.swadratna.swadratna_admin.data.repository.AuthRepository
import com.swadratna.swadratna_admin.data.repository.AuthRepositoryImpl
import com.swadratna.swadratna_admin.data.repository.CampaignRepository
import com.swadratna.swadratna_admin.data.repository.DashboardRepository
import com.swadratna.swadratna_admin.data.repository.StaffRepository
import com.swadratna.swadratna_admin.data.repository.StaffRepositoryImpl
import com.swadratna.swadratna_admin.data.model.ShiftTiming
import com.swadratna.swadratna_admin.utils.ApiConstants
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttp(
        authInterceptor: AuthInterceptor,
        headerInterceptor: HeaderInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(headerInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        sharedPrefsManager: SharedPrefsManager,
        authApiServiceProvider: Provider<AuthApiService>
    ): TokenAuthenticator {
        return TokenAuthenticator(sharedPrefsManager, authApiServiceProvider)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(sharedPrefsManager: SharedPrefsManager): AuthInterceptor {
        return AuthInterceptor(sharedPrefsManager)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideStoreApiService(retrofit: Retrofit): StoreApiService {
        return retrofit.create(StoreApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCampaignApi(retrofit: Retrofit): CampaignApi =
        retrofit.create(CampaignApi::class.java)

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideCampaignRepository(
        api: CampaignApi,
        io: CoroutineDispatcher
    ): CampaignRepository = CampaignRepository(api, io)

    @Provides
    @Singleton
    fun provideDashboardApi(retrofit: Retrofit): DashboardApi {
        return retrofit.create(DashboardApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDashboardRepository(api: DashboardApi): DashboardRepository {
        return DashboardRepository(api)
    }

    @Provides
    @Singleton
    fun provideAnalyticsApi(retrofit: Retrofit): AnalyticsApi =
        retrofit.create(AnalyticsApi::class.java)

    @Provides
    @Singleton
    fun provideMenuApi(retrofit: Retrofit): MenuApi =
        retrofit.create(MenuApi::class.java)

    @Provides
    @Singleton
    fun provideInventoryApi(retrofit: Retrofit): InventoryApi =
        retrofit.create(InventoryApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)
    
    @Provides
    @Singleton
    fun provideStaffApiService(retrofit: Retrofit): StaffApiService =
        retrofit.create(StaffApiService::class.java)

    @Provides
    @Singleton
    fun provideAssetApi(retrofit: Retrofit): AssetApi =
        retrofit.create(AssetApi::class.java)

    @Provides
    @Singleton
    fun provideSalesApi(retrofit: Retrofit): com.swadratna.swadratna_admin.data.remote.api.SalesApi =
        retrofit.create(com.swadratna.swadratna_admin.data.remote.api.SalesApi::class.java)

    @Provides
    @Singleton
    fun provideSalesRepository(api: com.swadratna.swadratna_admin.data.remote.api.SalesApi): com.swadratna.swadratna_admin.data.repository.SalesRepository =
        com.swadratna.swadratna_admin.data.repository.SalesRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideRestaurantApiService(retrofit: Retrofit): com.swadratna.swadratna_admin.data.remote.api.RestaurantApiService =
        retrofit.create(com.swadratna.swadratna_admin.data.remote.api.RestaurantApiService::class.java)

    @Provides
    @Singleton
    fun provideWithdrawalApi(retrofit: Retrofit): com.swadratna.swadratna_admin.data.remote.api.WithdrawalApi =
        retrofit.create(com.swadratna.swadratna_admin.data.remote.api.WithdrawalApi::class.java)

    @Provides
    @Singleton
    fun provideAttendanceApi(retrofit: Retrofit): com.swadratna.swadratna_admin.data.remote.api.AttendanceApi =
        retrofit.create(com.swadratna.swadratna_admin.data.remote.api.AttendanceApi::class.java)

    @Provides
    @Singleton
    fun provideCustomersApi(retrofit: Retrofit): CustomersApi =
        retrofit.create(CustomersApi::class.java)
}
