package com.example.models

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import kotlinx.serialization.Serializable

object VehicleTable: Table("Vehicle") {
    val id: Column<Long> = long("id")/*.autoIncrement()*/
    val make: Column<String> = varchar("make", 50)
    val model: Column<String> = varchar("model", 50)
    val year: Column<Int> = integer("year")
    val category: Column<String> = varchar("category", 50)
    val seats: Column<Int> = integer("seats")
    val range: Column<Double> = double("range")
    val beginOdometer: Column<Double> = double("beginOdometer")
    val endOdometer: Column<Double> = double("endOdometer")
    val licensePlate: Column<String> = varchar("licensePlate", 20)
    val status: Column<VehicleStatus> = enumerationByName("status", 20, VehicleStatus::class).default(VehicleStatus.AVAILABLE)
    val location: Column<String> = varchar("location", 100)
    val kilometerRate: Column<Double> = double("kilometerRate")
    val photoPath: Column<String> = text("photoPath").clientDefault { "[]" }

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Serializable
data class Vehicle constructor(
    val id: Long = 0L,
    val make: String,
    val model: String,
    val year: Int,
    val category: String,
    val seats: Int,
    val range: Double,
    val beginOdometer: Double,
    val endOdometer: Double,
    val licensePlate: String,
    val status: VehicleStatus = VehicleStatus.NULL,
    val location: String,
    val kilometerRate: Double,
    val photoPath: String = "[]"
)

@Serializable
enum class VehicleStatus {
    AVAILABLE,
    RENTED,
    MAINTENANCE,
    NULL
}