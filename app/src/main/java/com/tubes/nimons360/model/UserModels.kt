package com.tubes.nimons360.model

data class UserResponse(
    val data: UserDetail
)

data class UserDetail(
    val id: Int,
    val nim: String,
    val email: String,
    val fullName: String,
    val createdAt: String,
    val updatedAt: String
)

data class UpdateNameRequest(
    val fullName: String
)