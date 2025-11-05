package com.example.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.datetime.datetime

// Imports all the columns from the database and store in a usable object
object UserTable: Table("users") {
    val id: Column<Long> = long("id").autoIncrement()
    val firstName: Column<String> = varchar("first_name", 255)
    val lastName: Column<String> = varchar("last_name", 255)
    val username: Column<String> = varchar("username", 50).uniqueIndex()
    val address: Column<String> = varchar("address", 255)
    val role: Column<Role> = enumerationByName("role", 50, Role::class).default(Role.
    CUSTOMER)
    val phone: Column<String> = varchar("phone", 20)
    val password: Column<String> = varchar("password", 255)
    val email: Column<String> = varchar("email", 255)
    val rating: Column<Float?> = float("rating").nullable()
    val createdAt: Column<LocalDateTime?> = datetime("created_at").nullable()
    val birthDate: Column<LocalDateTime?> = datetime("birth_date").nullable()
    val driverLicenseNumber: Column<String> = varchar("driver_license_number", 50)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

// User
@Serializable
data class User @OptIn(ExperimentalTime::class) constructor(
    val id: Long = 0L,
    val firstName: String,
    val lastName: String,
    val username: String,
    val address: String,
    val role: Role = Role.CUSTOMER,
    val phone: String,
    val password: String,
    val email: String,
    val rating: Float? = 0.0f,
    val createdAt: LocalDateTime? = null,
    val birthDate: LocalDateTime? = null,
    val driverLicenseNumber: String
)

// Allowed roles
enum class Role {
    CUSTOMER,
    ADMIN
}

// Store the plaintext and hash in an object
data class Password (
    val hash: String,
    val plainText: String?
)