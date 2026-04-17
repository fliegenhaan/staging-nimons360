package com.tubes.nimons360.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val data: LoginData
)

data class LoginData(
    val token: String,
    val expiresAt: String,
    val user: UserData
)

data class UserData(
    val id: Int,
    val nim: String,
    val email: String,
    val fullName: String
)