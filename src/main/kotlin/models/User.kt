package com.example.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.datetime.datetime

object UserTable: Table("User") {
    val id: Column<Long> = long("id").autoIncrement()
    val name: Column<String> = varchar("name", 255)
    val role: Column<Role> = enumerationByName("role", 50, Role::class).default(Role.USER)
    val phone: Column<String> = varchar("phone", 20)
    val password: Column<String> = varchar("password", 255)
    val email: Column<String> = varchar("email", 255)
    val rating: Column<Float?> = float("rating").nullable()
    val createdAt: Column<LocalDateTime?> = datetime("createdAt").nullable()
    val birthDate: Column<LocalDateTime?> = datetime("birthDate").nullable()
    val driverLicenseNumber: Column<String> = varchar("driverLicenseNumber", 50)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
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
    val createdAt: LocalDateTime? = null,
    val birthDate: LocalDateTime? = null,
    val driverLicenseNumber: String
)

enum class Role {
    USER,
    ADMIN
}