package com.example.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object VehicleTable : Table("vehicles") {
    val id: Column<Long> = long("id").autoIncrement()

    // DDL: range_km INT
    val rangeKm: Column<Int?> = integer("range_km").nullable()

    val licensePlate: Column<String> = varchar("license_plate", 32).uniqueIndex()

    val status: Column<VehicleStatus> =
        enumerationByName("status", 20, VehicleStatus::class).default(VehicleStatus.AVAILABLE)

    // DDL: longitude/latitude DECIMAL(10,7)
    val longitude: Column<Double?> = double("longitude").nullable()
    val latitude: Column<Double?> = double("latitude").nullable()

    // DDL: owner_user_id BIGINT NULL, ON DELETE SET NULL
    val ownerId: Column<Long?> = long("owner_user_id").nullable()

    // DDL: begin_available/end_available TIMESTAMP (nullable by default)
    val beginAvailable: Column<LocalDateTime?> = datetime("begin_available").nullable()
    val endAvailable: Column<LocalDateTime?> = datetime("end_available").nullable()

    // DDL: price_per_day DECIMAL(10,2)
    val pricePerDay: Column<Double?> = double("price_per_day").nullable()

    val photoPath: Column<String?> = varchar("photo_path", 255).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Serializable
data class Vehicle(
    val id: Long = 0L,
    val rangeKm: Int? = null,
    val licensePlate: String,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val ownerId: Long? = null,
    val beginAvailable: LocalDateTime? = null,
    val endAvailable: LocalDateTime? = null,
    val pricePerDay: Double? = null,
    val photoPath: String? = null,
)

@Serializable
enum class VehicleStatus {
    AVAILABLE,
    RENTED,
    MAINTENANCE
}
