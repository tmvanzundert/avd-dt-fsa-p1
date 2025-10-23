package com.example.models

import org.jetbrains.exposed.v1.core.*

object PaymentTable: org.jetbrains.exposed.v1.core.Table("Payment") {
    val id: Column<Long> = long("id").autoIncrement()
    val amount: Column<Double> = double("amount")
    val currency: org.jetbrains.exposed.v1.core.Column<String> = varchar("currency", 3) // ISO 4217
    val provider: org.jetbrains.exposed.v1.core.Column<PaymentProvider> = enumerationByName("provider", 50, PaymentProvider::class)
    val status: org.jetbrains.exposed.v1.core.Column<PaymentStatus> = enumerationByName("status", 50, PaymentStatus::class)

    override val primaryKey: PrimaryKey = PrimaryKey(UserTable.id)
}

data class Payment(
    val id: Int,
    val amount: Double,
    val currency: String, // ISO 4217
    val provider: PaymentProvider,
    val status: PaymentStatus
)

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}

enum class PaymentProvider {
    IDEAL,
    KLARNA,
    PAYPAL
}