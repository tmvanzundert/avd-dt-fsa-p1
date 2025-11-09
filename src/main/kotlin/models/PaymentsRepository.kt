package com.example.models

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

interface PaymentsRepository : CrudRepository<Payment, Long> {

    fun findByReservation(reservationId: Long): List<Payment>

    fun createPayment(
        reservationId: Long,
        amount: Double,
        currency: Char,
        provider: String,
        status: String = "AUTHORIZED",
        authorizedAt: LocalDateTime = LocalDateTime(2025, 1, 1, 0, 0, 0),
        capturedAt: LocalDateTime? = null,
        refundedAt: LocalDateTime? = null
    ): Payment

    fun capturePayment(paymentId: Long, capturedAt: LocalDateTime = LocalDateTime(2025, 1, 1, 0, 0, 0)): Payment

    fun refundPayment(paymentId: Long, refundedAt: LocalDateTime = LocalDateTime(2025, 1, 1, 0, 0, 0)): Payment
}

class PaymentsDao :
    CrudDAO<Payment, Long, PaymentsTable>(PaymentsTable),
    PaymentsRepository {

    override fun getEntity(row: ResultRow): Payment {
        return Payment(
            id = row[PaymentsTable.id],
            reservation_id = row[PaymentsTable.reservation],
            amount = row[PaymentsTable.amount],
            currency = row[PaymentsTable.currency],
            provider = row[PaymentsTable.provider],
            status = row[PaymentsTable.status],
            authorized_at = row[PaymentsTable.authorized_at],
            captured_at = row[PaymentsTable.captured_at],
            refunded_at = row[PaymentsTable.refunded_at],
        )
    }

    override fun createEntity(entity: Payment, statement: UpdateBuilder<Int>) {
        statement[PaymentsTable.id] = entity.id
        statement[PaymentsTable.reservation] = entity.reservation_id
        statement[PaymentsTable.amount] = entity.amount
        statement[PaymentsTable.currency] = entity.currency
        statement[PaymentsTable.provider] = entity.provider
        statement[PaymentsTable.status] = entity.status
        statement[PaymentsTable.authorized_at] = entity.authorized_at
        statement[PaymentsTable.captured_at] = entity.captured_at
        statement[PaymentsTable.refunded_at] = entity.refunded_at
    }

    override fun findByReservation(reservationId: Long): List<Payment> {
        return findAll().filter { it.reservation_id == reservationId }
    }

    override fun createPayment(
        reservationId: Long,
        amount: Double,
        currency: Char,
        provider: String,
        status: String,
        authorizedAt: LocalDateTime,
        capturedAt: LocalDateTime?,
        refundedAt: LocalDateTime?
    ): Payment {
        val payment = Payment(
            id = 0L,
            reservation_id = reservationId,
            amount = amount,
            currency = currency,
            provider = provider,
            status = status,
            authorized_at = authorizedAt,
            captured_at = capturedAt ?: authorizedAt,
            refunded_at = refundedAt ?: authorizedAt,
        )
        create(payment)
        val inserted = findAll().maxByOrNull { it.id }!!
        return inserted
    }

    override fun capturePayment(paymentId: Long, capturedAt: LocalDateTime): Payment {
        val payment = findById(paymentId) ?: throw Exception("Payment not found")
        updateProperty(paymentId, "status", "CAPTURED")
        updateProperty(paymentId, "captured_at", capturedAt)
        return payment.copy(status = "CAPTURED", captured_at = capturedAt)
    }

    override fun refundPayment(paymentId: Long, refundedAt: LocalDateTime): Payment {
        val payment = findById(paymentId) ?: throw Exception("Payment not found")
        updateProperty(paymentId, "status", "REFUNDED")
        updateProperty(paymentId, "refunded_at", refundedAt)
        return payment.copy(status = "REFUNDED", refunded_at = refundedAt)
    }
}
