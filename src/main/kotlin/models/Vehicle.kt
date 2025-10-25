package com.example.models

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.datetime.*

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
    val status: Column<VehicleStatus> = enumerationByName("status", 20, VehicleStatus::class).default(VehicleStatus.NULL)
    val location: Column<String> = varchar("location", 100)
    val price: Column<Double> = double("price")
    val photoPath: Column<String> = text("photoPath").clientDefault { "[]" }
    val beginReservation: Column<LocalDateTime?> = datetime("beginReservation").nullable()
    val endReservation: Column<LocalDateTime?> = datetime("endReservation").nullable()
    val totalYearlyUsageKilometers: Column<Double> = double("totalYearlyUsageKilometers")

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
    val beginOdometer: Double,
    val endOdometer: Double,
    val licensePlate: String,
    val status: VehicleStatus = VehicleStatus.NULL,
    val location: String,
    val price: Double,
    val photoPath: String = "[]",
    val beginReservation: LocalDateTime?,
    val endReservation: LocalDateTime?,
    var totalYearlyUsageKilometers: Double
)

@Serializable
enum class VehicleStatus {
    AVAILABLE,
    RENTED,
    MAINTENANCE,
    NULL
}