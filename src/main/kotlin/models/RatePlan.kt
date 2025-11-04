package com.example.models

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import kotlinx.serialization.Serializable

object RatePlanTable: Table("rate_plans") {
    val id: Column<Long> = long("id").autoIncrement()
    val rentalContractId: Column<Long> = long("rental_contract_id")
    val name: Column<String> = varchar("name", 120)
    val pricePerDay: Column<Double> = double("price_per_day")
    val pricePerKm: Column<Double> = double("price_per_km")
    val deposit: Column<Double> = double("deposit")
    val cancellationPolicy: Column<String> = text("cancellation_policy")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Serializable
data class RatePlan (
    val id: Long,
    val rentalContractId: Long,
    val name: String,
    val pricePerDay: Double,
    val pricePerKm: Double,
    val deposit: Double,
    val cancellationPolicy: String
)