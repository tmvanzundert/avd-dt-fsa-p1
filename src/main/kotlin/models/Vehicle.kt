package com.example.models

import org.jetbrains.exposed.v1.core.Table

object VehicleTable: Table("Vehicle") {
    val id: Column<Long> = long("id").autoIncrement().primaryKey()
    val make: Column<String> = varchar("make", 50)
    val model: Column<String> = varchar("model", 50)
    val year: Column<Int> = integer("year")
    val category: Column<String> = varchar("category", 50)
    val seats: Column<Int> = integer("seats")
    val range: Column<Double> = double("range")
    val odometer: Column<Double> = double("odometer")
    val licensePlate: Column<String> = varchar("licensePlate", 20)
    val status: Column<VehicleStatus> = enumerationByName("status", 20, VehicleStatus::class).default(VehicleStatus.AVAILABLE)
    val location: Column<String> = varchar("location", 100)
    val hourlyRate: Column<Double> = double("hourlyRate")
    val customRate: Column<Int> = integer("customRate")
}

data class Vehicle constructor(
    val id: Long = 0L,
    val make: String,
    val model: String,
    val year: Int,
    val category: String,
    val seats: Int,
    val range: Double,
    val odometer: Double,
    val licensePlate: String,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    val location: String,
    val hourlyRate: Double,
    val customRate: Int
)

enum class VehicleStatus {
    AVAILABLE,
    RENTED,
    MAINTENANCE
}