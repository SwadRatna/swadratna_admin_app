package com.swadratna.swadratna_admin.data.remote.api

import android.os.Build
import androidx.annotation.RequiresApi
import com.swadratna.swadratna_admin.data.model.RefreshTokenRequest
import com.swadratna.swadratna_admin.data.model.RefreshTokenResponse
import com.swadratna.swadratna_admin.utils.SharedPrefsManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

@RequiresApi(Build.VERSION_CODES.O)
class TokenAuthenticator @Inject constructor(
    private val sharedPrefsManager: SharedPrefsManager,
    private val authApiServiceProvider: Provider<AuthApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Simply return null for now as we're not using refresh token functionality
        return null
        
        /* Refresh token functionality commented out as requested
        val refreshToken = sharedPrefsManager.getRefreshToken() ?: return null

        return synchronized(this) {
            val currentToken = sharedPrefsManager.getAuthToken()
            val originalRequestToken = response.request.header("Authorization")?.substringAfter("Bearer ")

            if (currentToken != null && currentToken != originalRequestToken) {
                return@synchronized newRequestWithToken(response.request, currentToken)
            }

            val newTokens: RefreshTokenResponse? = runBlocking {
                try {
                    authApiServiceProvider.get().refreshToken(RefreshTokenRequest(refreshToken))
                } catch (e: Exception) {
                    null
                }
            }

            if (newTokens != null) {
                sharedPrefsManager.saveAuthToken(newTokens.token)
                sharedPrefsManager.saveRefreshToken(newTokens.refreshToken)
                newRequestWithToken(response.request, newTokens.token)
            } else {
                sharedPrefsManager.clearTokens()
                null
            }
        }
        */
    }

    private fun newRequestWithToken(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }
}