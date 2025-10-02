package com.swadratna.swadratna_admin.data.remote.api

import android.os.Build
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

        sharedPrefsManager.getAuthToken()?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}