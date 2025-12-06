package com.swadratna.swadratna_admin.ui.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_admin.data.model.LoginRequest
import com.swadratna.swadratna_admin.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            if (email.isEmpty() || !email.contains("@")) {
                _loginState.value = LoginState(errorMessage = "Please enter a valid email")
                return@launch
            }
            
            if (password.isEmpty() || password.length < 6) {
                _loginState.value = LoginState(errorMessage = "Password must be at least 6 characters")
                return@launch
            }

            val loginRequest = LoginRequest(email, password)
            authRepository.login(loginRequest).fold(
                onSuccess = { 
                    _loginState.value = LoginState(isSuccess = true)
                },
                onFailure = { exception ->
                    val errorMessage = when (exception) {
                        is HttpException -> {
                            when (exception.code()) {
                                400 -> "Something seems off in the request. Please review your details and try again."
                                401 -> "The email or password you entered doesn’t match. Please try again."
                                403 -> "You don’t have permission to do this action."
                                404 -> "We couldn’t reach the server. The requested page wasn’t found."
                                408 -> "The request took too long. Please check your connection and try again."
                                500, 502, 503, 504 -> "The server is having trouble right now. Please try again in a little while."
                                else -> "Something went wrong. (Error code: ${exception.code()})"
                            }
                        }
                        is IOException -> "No internet connection. Please check your network."
                        else -> exception.message ?: "An unexpected error occurred"
                    }
                    _loginState.value = LoginState(errorMessage = errorMessage)
                }
            )
        }
    }
}

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)