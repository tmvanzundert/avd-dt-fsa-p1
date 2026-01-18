package com.example.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.datetime

// Imports all the columns from the database and store in a usable object
object UserTable: Table("users") {
    val id: Column<Long> = long("id").autoIncrement()
    val firstName: Column<String> = varchar("first_name", 100)
    val lastName: Column<String> = varchar("last_name", 100)
    val username: Column<String> = varchar("username", 80).uniqueIndex()
    val password: Column<String> = varchar("password", 255)
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val rating: Column<Float?> = float("rating").nullable()
    val phone: Column<String?> = varchar("phone", 32).nullable()
    val role: Column<Role> = enumerationByName("role", 20, Role::class).default(Role.CUSTOMER)
    val createdAt: Column<LocalDateTime?> = datetime("created_at").nullable()

    // DDL: birth_date DATE
    val birthDate: Column<LocalDate?> = date("birth_date").nullable()

    val driverLicenseNumber: Column<String?> = varchar("driver_license_number", 64).nullable()
    val address: Column<String?> = varchar("address", 255).nullable()
    val avatarPath: Column<String?> = varchar("avatar_path", 255).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

// User
@Serializable
data class User constructor(
    val id: Long = 0L,
    val firstName: String,
    val lastName: String,
    val username: String,
    val address: String? = null,
    val role: Role = Role.CUSTOMER,
    val phone: String? = null,
    val password: String,
    val email: String,
    val rating: Float? = 5.0f,
    val createdAt: LocalDateTime? = null,
    val birthDate: LocalDate? = null,
    val driverLicenseNumber: String? = null,
    val avatarPath: String? = null,
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