package com.example.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object ReservationTable: Table("reservations") {
    val id: Column<Long> = long("id").autoIncrement()

    // DDL: user_id BIGINT NULL, ON DELETE SET NULL
    val userId: Column<Long?> = long("user_id").nullable()

    // DDL: vehicle_id BIGINT (RESTRICT)
    val vehicleId: Column<Long?> = long("vehicle_id").nullable()

    val startAt: Column<LocalDateTime?> = datetime("start_at").nullable()
    val endAt: Column<LocalDateTime?> = datetime("end_at").nullable()

    val status: Column<ReservationStatus> =
        enumerationByName("status", 20, ReservationStatus::class).default(ReservationStatus.PENDING)

    // DDL: DECIMAL(12,2)
    val totalAmount: Column<Double?> = double("total_amount").nullable()

    // NOTE: The database schema (database/DDL.sql) does not define photo columns for reservations.
    // Do not add them here unless you also add them to the DB, otherwise Exposed will SELECT them and crash.

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Serializable
data class Reservations(
    val id: Long = 0L,
    val userId: Long? = null,
    val vehicleId: Long? = null,
    val startAt: LocalDateTime? = null,
    val endAt: LocalDateTime? = null,
    val status: ReservationStatus = ReservationStatus.PENDING,
    val totalAmount: Double? = null,
)

@Serializable
enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
}
