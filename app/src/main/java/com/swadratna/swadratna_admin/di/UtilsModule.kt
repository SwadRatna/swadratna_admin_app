package com.swadratna.swadratna_admin.di

import com.swadratna.swadratna_admin.utils.JwtUtils
import com.swadratna.swadratna_admin.utils.SessionManager
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    @Singleton
    fun provideSessionManager(
        sharedPrefsManager: SharedPrefsManager,
        jwtUtils: JwtUtils
    ): SessionManager {
        return SessionManager(sharedPrefsManager, jwtUtils)
    }
}