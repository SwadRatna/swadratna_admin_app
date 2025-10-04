package com.swadratna.swadratna_admin.data.remote.api

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
class AuthInterceptor @Inject constructor(
    private val sharedPrefsManager: SharedPrefsManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        val token = sharedPrefsManager.getAuthToken()
        if (token != null) {
            Log.d("AuthInterceptor", "Adding Bearer token to request: ${token.take(10)}...")
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } else {
            Log.w("AuthInterceptor", "No auth token found in SharedPreferences")
        }

        return chain.proceed(requestBuilder.build())
    }
}