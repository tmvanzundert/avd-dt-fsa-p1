package com.example.models

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

enum class Type {
    AVAILABLE,
    RENTED,
    MAINTENANCE
}

object VehicleStatusTable : Table("vehicle_status") {
    val id: Column<Long> = long("id").autoIncrement()
    val type: Column<Type> = enumerationByName("type", 40, Type::class)
    val ownerId: Column<Long?> = long("owner_id").nullable()
    val ownerType: Column<String?> = varchar("owner_type", 40).nullable()
    val uri: Column<String?> = varchar("uri", 255).nullable()
    val checksum: Column<String?> = varchar("checksum", 128).nullable()
    val createdAt: Column<LocalDateTime?> = datetime("created_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

data class VehicleStatus(
    val id: Long,
    val type: Type,
    val ownerId: Long?,
    val ownerType: String?,
    val uri: String?,
    val checksum: String?,
    val createdAt: LocalDateTime?
)