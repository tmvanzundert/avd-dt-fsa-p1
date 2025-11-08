package com.example.models

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object PaymentsTable: Table("Payment") {
    val id: Column<Long> = long("id").autoIncrement()
    val reservation: Column<Long> = long("reservation")
    val amount: Column<Double> = double("amount")
    val currency: Column<Char> = char("currency")
    val provider: Column<String> = varchar("provider_name", 40)
    val status: Column<String> = varchar("status", 32)
    val authorized_at: Column<LocalDateTime> = datetime("authorized_at")
    val captured_at: Column<LocalDateTime> = datetime("captured_at")
    val refunded_at: Column<LocalDateTime> = datetime("refunded_at")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

data class Payment(
    val id: Long,
    val reservation_id: Long,
    val amount: Double,
    val currency: Char,
    val provider: String,
    val status: String,
    val authorized_at: LocalDateTime,
    val captured_at: LocalDateTime,
    val refunded_at: LocalDateTime
)