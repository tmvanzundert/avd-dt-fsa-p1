package com.example.models

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.datetime.datetime
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDateTime

object VehicleTable : Table("vehicles") {
    val id: Column<Long> = long("id").autoIncrement()
    val make: Column<String> = varchar("make", 50)
    val model: Column<String> = varchar("model", 50)
    val year: Column<Int> = integer("year")
    val category: Column<String> = varchar("category", 50)
    val seats: Column<Int> = integer("seats")
    val range: Column<Double> = double("range_km")
    val licensePlate: Column<String> = varchar("license_plate", 20)
    val status: Column<VehicleStatus> = enumerationByName("status", 20, VehicleStatus::class).default(VehicleStatus.AVAILABLE)
    val location: Column<Long> = long("location_id")
    val ownerId: Column<Long> = long("owner_user_id")
    val photoPath: Column<String> = text("photo_path").clientDefault { "[]" }
    val totalYearlyUsageKilometers: Column<Long> = long("total_yearly_kilometers")
    val tco: Column<Double?> = double("tco").default(0.0) as Column<Double?>
    val beginAvailable: Column<LocalDateTime> = datetime("begin_available")
    val endAvailable: Column<LocalDateTime> = datetime("end_available")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Serializable
data class Vehicle @OptIn(ExperimentalTime::class) constructor(
    val id: Long = 0L,
    val make: String,
    val model: String,
    val year: Int,
    val category: String,
    val seats: Int,
    val range: Double,
    val licensePlate: String,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    val location: Long,
    val ownerId: Long,
    val photoPath: String = "[]",
    val totalYearlyUsageKilometers: Long = 0,
    val tco: Double?,
    val beginAvailable: LocalDateTime,
    val endAvailable: LocalDateTime
)

@Serializable
enum class VehicleStatus {
    AVAILABLE,
    RENTED,
    MAINTENANCE
}
