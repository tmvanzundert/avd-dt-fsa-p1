package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table

object PaymentsTable: Table("payments") {
    val id: Column<Long> = long("id").autoIncrement()

    val reservationId: Column<Long?> = long("reservation_id").nullable()

    // DDL: DECIMAL(11,2)
    val amount: Column<Double?> = double("amount").nullable()

    // DDL: CHAR(3)
    val currency: Column<String?> = char("currency", 3).nullable()

    val provider: Column<PaymentProvider?> =
        enumerationByName("provider", 20, PaymentProvider::class).nullable()

    val status: Column<PaymentStatus> =
        enumerationByName("status", 20, PaymentStatus::class).default(PaymentStatus.AUTHORIZED)

    val deposit: Column<Double?> = double("deposit").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Serializable
data class Payment(
    val id: Long = 0L,
    val reservationId: Long? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val provider: PaymentProvider? = null,
    val status: PaymentStatus = PaymentStatus.AUTHORIZED,
    val deposit: Double? = null,
)

@Serializable
enum class PaymentProvider {
    STRIPE,
    PAYPAL,
    IDEAL,
    BANCONTACT,
}

@Serializable
enum class PaymentStatus {
    AUTHORIZED,
    CAPTURED,
    REFUNDED,
    FAILED,
}
