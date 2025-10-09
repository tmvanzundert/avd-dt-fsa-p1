package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val id: Long = 0L,
    val role: Role = Role.USER,
    val username: String,
    val password: String,
    val email: String,
    val rating: Float = 0.0f
)

enum class Role {
    USER,
    ADMIN
}