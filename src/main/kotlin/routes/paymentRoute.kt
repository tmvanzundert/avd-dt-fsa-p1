package com.example.routes

import com.example.models.Payment
import com.example.models.PaymentsDao
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.time.ExperimentalTime

class PaymentRoute(
    entityClass: KClass<Payment>,
    override val dao: PaymentsDao
) : ModelRoute<PaymentsDao, Payment>("payment", entityClass)

@Serializable
data class CreatePaymentRequest(
    val reservationId: Long,
    val amount: Double,
    val currency: Char,
    val provider: String
)

@Serializable
data class CapturePaymentRequest(
    val capturedAt: LocalDateTime? = null
)

@Serializable
data class RefundPaymentRequest(
    val refundedAt: LocalDateTime? = null
)

@OptIn(ExperimentalTime::class)
fun Route.paymentRoutes(paymentsDao: PaymentsDao) {
    val paymentRoute = PaymentRoute(Payment::class, paymentsDao)

    paymentRoute.apply {
        list()      // GET   /payment
        getById()   // GET   /payment/{id}
        create()    // POST  /payment   (generic; you can disable this if you only want the custom one below)
        update()    // PUT   /payment/{id}
        delete()    // DELETE /payment/{id}
    }


    post("/payment/create") {
        val body = call.receive<CreatePaymentRequest>()

        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC)

        val payment = paymentsDao.createPayment(
            reservationId = body.reservationId,
            amount = body.amount,
            currency = body.currency,
            provider = body.provider,
            status = "AUTHORIZED",
            authorizedAt = now,
            capturedAt = null,
            refundedAt = null
        )

        call.respond(payment)
    }

    // Capture a payment
    post("/payment/{id}/capture") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respondText("Invalid payment id", status = io.ktor.http.HttpStatusCode.BadRequest)

        val body = call.runCatching { receive<CapturePaymentRequest>() }.getOrNull()
        val capturedAt = body?.capturedAt ?: kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC)

        val updated = paymentsDao.capturePayment(id, capturedAt)
        call.respond(updated)
    }

    // Refund a payment
    post("/payment/{id}/refund") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respondText("Invalid payment id", status = io.ktor.http.HttpStatusCode.BadRequest)

        val body = call.runCatching { receive<RefundPaymentRequest>() }.getOrNull()
        val refundedAt = body?.refundedAt ?: kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC)

        val updated = paymentsDao.refundPayment(id, refundedAt)
        call.respond(updated)
    }

    // Get all payments for a reservation
    get("/payment/reservation/{reservationId}") {
        val reservationId = call.parameters["reservationId"]?.toLongOrNull()
            ?: return@get call.respondText("Invalid reservation id", status = io.ktor.http.HttpStatusCode.BadRequest)

        val payments = paymentsDao.findByReservation(reservationId)

        if (payments.isEmpty()) {
            call.respond(io.ktor.http.HttpStatusCode.NoContent)
        } else {
            call.respond(payments)
        }
    }
}
