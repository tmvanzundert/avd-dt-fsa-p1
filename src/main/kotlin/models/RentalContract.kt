package com.example.models

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object RentalContractTable: Table("rental_contracts") {
    val id: Column<Long> = long("id").autoIncrement()
    val vehicleId: Column<Long> = long("vehicle_id")
    val pickupOdometer: Column<Long> = long("pickup_odometer")
    val dropoffOdometer: Column<Long> = long("dropoff_odometer")
    val pickupTime: Column<LocalDateTime> = datetime("pickup_time")
    val returnTime: Column<LocalDateTime> = datetime("return_time")
    val signedAt: Column<LocalDateTime> = datetime("signed_at")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Serializable
data class RentalContract (
    val id: Long,
    val vehicleId: Long,
    val pickupOdometer: Long,
    val dropoffOdometer: Long,
    val pickupTime: LocalDateTime,
    val returnTime: LocalDateTime,
    val signedAt: LocalDateTime
)