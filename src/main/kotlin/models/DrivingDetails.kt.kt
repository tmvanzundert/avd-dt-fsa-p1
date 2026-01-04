package com.example.models

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object DrivingTable: Table("driving_details") {
    val id = long("id").autoIncrement()
    val renterId = long("renter_id")
    val vehicleId = long("vehicle_id")
    val startTime = datetime("start_time")
    val endTime = datetime("end_time")
    val startLongitude = double("start_longitude")
    val startLatitude = double("start_latitude")
    val endLongitude = double("end_longitude")
    val endLatitude = double("end_latitude")

    override val primaryKey = PrimaryKey(id)
}

data class DrivingDetails(
    val id: Long = 0L,
    val renterId: Long,
    val vehicleId: Long,
    val startTime: kotlinx.datetime.LocalDateTime,
    val endTime: kotlinx.datetime.LocalDateTime,
    val startLongitude: Double,
    val startLatitude: Double,
    val endLongitude: Double,
    val endLatitude: Double,
)