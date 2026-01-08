package com.example.routes

import com.example.models.Payment
import com.example.models.PaymentProvider
import com.example.models.PaymentStatus
import com.example.models.PaymentsDao
import com.example.models.ReservationsDao
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

class PaymentRoute(
    entityClass: KClass<Payment>,
    override val dao: PaymentsDao
) : ModelRoute<PaymentsDao, Payment>("payment", entityClass)

@Serializable
data class CreatePaymentRequest(
    val reservationId: Long,
    val amount: Double,
    val currency: String,
    val provider: PaymentProvider,
    val deposit: Double? = null,
)

@Serializable
data class CalcTotalAmountRequest(
    val reservationId: Long,
    val startAt: String? = null,
)

@Serializable
data class CalcTotalAmountResponse(
    val reservationId: Long,
    val totalAmount: Double,
)

fun Route.paymentRoutes(paymentsDao: PaymentsDao) {
    val paymentRoute = PaymentRoute(Payment::class, paymentsDao)

    paymentRoute.apply {
        list()
        getById()
        create()
        update()
        delete()
    }

    // Create a new payment, deposit
    post("/payment/deposit") {
        val body = call.receive<CreatePaymentRequest>()

        val payment = paymentsDao.createPayment(
            reservationId = body.reservationId,
            amount = body.amount,
            currency = body.currency,
            provider = body.provider,
            status = PaymentStatus.AUTHORIZED,
            deposit = body.deposit,
        )

        call.respond(payment)
    }

    // Total cost is calculated at the end of the renting period, if reservation status is COMPLETED, calculate total cost and create payment
    // accepts a date range, creates a payment for the total amount
    post("/payment/complete") {

    }

    post("/payment/create") {
        val body = call.receive<CreatePaymentRequest>()

        val payment = paymentsDao.createPayment(
            reservationId = body.reservationId,
            amount = body.amount,
            currency = body.currency,
            provider = body.provider,
            status = PaymentStatus.AUTHORIZED,
            deposit = body.deposit,
        )

        call.respond(payment)
    }

    post("/payment/{id}/capture") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respondText(
                "Invalid payment id",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )

        val updated = paymentsDao.capturePayment(id)
        call.respond(updated)
    }

    post("/payment/{id}/refund") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respondText(
                "Invalid payment id",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )

        val updated = paymentsDao.refundPayment(id)
        call.respond(updated)
    }

    // Get all payments for a reservation
    get("/payment/reservation/{reservationId}") {
        val reservationId = call.parameters["reservationId"]?.toLongOrNull()
            ?: return@get call.respondText(
                "Invalid reservation id",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )

        val payments = paymentsDao.findByReservation(reservationId)

        if (payments.isEmpty()) {
            call.respond(io.ktor.http.HttpStatusCode.NoContent)
        } else {
            call.respond(payments)
        }
    }

    // TEMPORARY: debug endpoint to verify the total-amount calculation over HTTP.
    // Remove once a proper payment completion flow exists.
    post("/payment/_debug/calc-total") {
        val body = call.receive<CalcTotalAmountRequest>()

        val reservation = ReservationsDao().findById(body.reservationId)
            ?: return@post call.respondText(
                "Reservation not found",
                status = io.ktor.http.HttpStatusCode.NotFound
            )

        val startAt = when {
            body.startAt != null -> {
                try {
                    kotlinx.datetime.LocalDateTime.parse(body.startAt)
                } catch (e: Exception) {
                    return@post call.respondText(
                        "Invalid startAt; expected ISO-8601 LocalDateTime (e.g. 2026-01-04T10:00:00). Error: ${'$'}e",
                        status = io.ktor.http.HttpStatusCode.BadRequest
                    )
                }
            }
            reservation.startAt != null -> reservation.startAt
            else -> return@post call.respondText(
                "Reservation startAt is missing",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )
        }

        val total = paymentsDao.calculateTotalAmount(body.reservationId, startAt)
        call.respond(CalcTotalAmountResponse(body.reservationId, total))
    }
}
