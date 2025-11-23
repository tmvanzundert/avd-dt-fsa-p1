package com.example.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object ReservationTable: Table("reservations") {
    val id: Column<Long> = long("id").autoIncrement()
    val user_id: Column<Long> = long("user_id")
    val vehicle_id: Column<Long> = long("vehicle_id")
    val rate_plan_id: Column<Long> = long("rate_plan_id")
    val renter_id: Column<Long> = long("renter_id")
    val start_at: Column<LocalDateTime> = datetime("start_at")
    val end_at: Column<LocalDateTime> = datetime("end_at")
    val status: Column<String> = varchar("status", 255)
    val total_amount: Column<Double> = double("total_amount")
    val pickup_location_id: Column<Long> = long("pickup_location_id")
    val dropoff_location_id: Column<Long> = long("dropoff_location_id")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Serializable
data class Reservations(
    val id: Long,
    val user_id: Long,
    val vehicle_id: Long,
    val rate_plan_id: Long,
    val renter_id: Long,
    val start_at: LocalDateTime,
    val end_at: LocalDateTime,
    val status: String,
    val total_amount: Double,
    val pickup_location_id: Long,
    val dropoff_location_id: Long,
)