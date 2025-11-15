package com.swadratna.swadratna_admin.data.remote.api

import com.swadratna.swadratna_admin.utils.ApiConstants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class HeaderInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Create a new request with the required headers
        val newRequest = originalRequest.newBuilder()
            .header("Content-Type", ApiConstants.CONTENT_TYPE)
            .header("Accept", ApiConstants.ACCEPT)
            .header("X-Key", ApiConstants.API_KEY)
            .method(originalRequest.method, originalRequest.body)
            .build()
        
        return chain.proceed(newRequest)
    }
}