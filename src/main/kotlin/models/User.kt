package com.example.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object UserTable: Table("User") {
    val id = long("id").autoIncrement().primaryKey()
    val name = varchar("name", 255)
    val role = enumerationByName("role", 50, Role::class).default(Role.USER)
    val phone = varchar("phone", 20)
    val password = varchar("password", 255)
    val email = varchar("email", 255)
    val rating = float("rating").nullable()
    val createdAt = datetime("createdAt").nullable()
    val birthDate = datetime("birthDate").nullable()
    val driverLicenseNumber = varchar("driverLicenseNumber", 50)
}

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