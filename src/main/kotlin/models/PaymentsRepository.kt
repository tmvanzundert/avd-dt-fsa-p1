package com.example.models

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

interface PaymentsRepository : CrudRepository<Payment, Long> {

    fun findByReservation(reservationId: Long): List<Payment>

    fun createPayment(
        reservationId: Long,
        amount: Double,
        currency: String,
        provider: PaymentProvider,
        status: PaymentStatus = PaymentStatus.AUTHORIZED,
        deposit: Double? = null,
    ): Payment

    fun capturePayment(paymentId: Long): Payment

    fun refundPayment(paymentId: Long): Payment
}

class PaymentsDao :
    CrudDAO<Payment, Long, PaymentsTable>(PaymentsTable),
    PaymentsRepository {

    override fun getEntity(row: ResultRow): Payment {
        return Payment(
            id = row[PaymentsTable.id],
            reservationId = row[PaymentsTable.reservationId],
            amount = row[PaymentsTable.amount],
            currency = row[PaymentsTable.currency],
            provider = row[PaymentsTable.provider],
            status = row[PaymentsTable.status],
            deposit = row[PaymentsTable.deposit],
        )
    }

    override fun createEntity(entity: Payment, statement: UpdateBuilder<Int>) {
        statement[PaymentsTable.reservationId] = entity.reservationId
        statement[PaymentsTable.amount] = entity.amount
        statement[PaymentsTable.currency] = entity.currency
        statement[PaymentsTable.provider] = entity.provider
        statement[PaymentsTable.status] = entity.status
        statement[PaymentsTable.deposit] = entity.deposit
    }

    override fun findByReservation(reservationId: Long): List<Payment> {
        return findAll().filter { it.reservationId == reservationId }
    }

    override fun createPayment(
        reservationId: Long,
        amount: Double,
        currency: String,
        provider: PaymentProvider,
        status: PaymentStatus,
        deposit: Double?,
    ): Payment {
        val payment = Payment(
            reservationId = reservationId,
            amount = amount,
            currency = currency,
            provider = provider,
            status = status,
            deposit = deposit,
        )
        create(payment)
        return findAll().maxByOrNull { it.id }!!
    }

    override fun capturePayment(paymentId: Long): Payment {
        val payment = findById(paymentId) ?: throw Exception("Payment not found")
        updateProperty(paymentId, "status", PaymentStatus.CAPTURED)
        return payment.copy(status = PaymentStatus.CAPTURED)
    }

    override fun refundPayment(paymentId: Long): Payment {
        val payment = findById(paymentId) ?: throw Exception("Payment not found")
        updateProperty(paymentId, "status", PaymentStatus.REFUNDED)
        return payment.copy(status = PaymentStatus.REFUNDED)
    }
}
