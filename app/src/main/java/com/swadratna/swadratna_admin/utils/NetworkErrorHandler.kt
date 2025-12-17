package com.swadratna.swadratna_admin.utils

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkErrorHandler {
    fun getErrorMessage(e: Throwable, defaultMessage: String = "An unexpected error occurred"): String {
        return when (e) {
            is UnknownHostException, is ConnectException, is SocketTimeoutException -> {
                "No internet connection. Please check your network."
            }
            is IOException -> {
                "Network error. Please check your connection."
            }
            is HttpException -> {
                when (e.code()) {
                    401 -> "Session expired. Please login again."
                    403 -> "Access denied."
                    404 -> "Resource not found."
                    500, 502, 503, 504 -> "Server error. Please try again later."
                    else -> "Request failed (${e.code()})."
                }
            }
            else -> e.message ?: defaultMessage
        }
    }
}
