package com.swadratna.swadratna_admin.data.model

data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val user: User
)

data class User(
    val id: String,
    val name: String,
    val email: String
)