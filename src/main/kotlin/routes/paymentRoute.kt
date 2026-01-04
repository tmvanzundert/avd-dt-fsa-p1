package com.example.routes

import com.example.models.Payment
import com.example.models.PaymentProvider
import com.example.models.PaymentStatus
import com.example.models.PaymentsDao
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
    /** DDL: CHAR(3) */
    val currency: String,
    /** DDL: ENUM('STRIPE','PAYPAL','IDEAL','BANCONTACT') */
    val provider: PaymentProvider,
    val deposit: Double? = null,
)

fun Route.paymentRoutes(paymentsDao: PaymentsDao) {
    val paymentRoute = PaymentRoute(Payment::class, paymentsDao)

    paymentRoute.apply {
        list()      // GET   /payment
        getById()   // GET   /payment/{id}
        create()    // POST  /payment
        update()    // PUT   /payment/{id}
        delete()    // DELETE /payment/{id}
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

    // Capture a payment (DDL has no captured_at, so we only flip status)
    post("/payment/{id}/capture") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respondText(
                "Invalid payment id",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )

        val updated = paymentsDao.capturePayment(id)
        call.respond(updated)
    }

    // Refund a payment (DDL has no refunded_at, so we only flip status)
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
}
