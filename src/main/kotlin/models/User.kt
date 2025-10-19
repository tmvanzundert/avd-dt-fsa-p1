package com.example.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class User @OptIn(ExperimentalTime::class) constructor(
    val id: Long = 0L,
    val name: String,
    val role: Role = Role.USER,
    val phone: String,
    val password: String,
    val email: String,
    val rating: Float? = 0.0f,
    val createdAt: Instant? = null,
    val birthDate: Instant? = null,
    val driverLicenseNumber: String
)

enum class Role {
    USER,
    ADMIN
}