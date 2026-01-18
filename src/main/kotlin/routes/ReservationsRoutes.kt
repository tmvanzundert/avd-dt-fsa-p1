package com.example.routes

import com.example.models.ReservationStatus
import com.example.models.Reservations
import com.example.models.ReservationsDao
import com.example.models.User
import com.example.models.UserDao
import com.example.models.VehicleDao
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

class ReservationsRoute(entityClass: KClass<Reservations>, override val dao: ReservationsDao) : ModelRoute<ReservationsDao, Reservations>("reservation", entityClass) {

}

fun Route.reservationsRoutes(
    reservationsDao: ReservationsDao,
    @Suppress("UNUSED_PARAMETER") userDao: UserDao,

) {
    val reservationRoute = ReservationsRoute(Reservations::class, reservationsDao)

    reservationRoute.apply {
        list()
        getById()
        // NOTE: we handle POST /reservation ourselves below (custom request body + better errors)
        update()
        delete()
    }

    // Create a reservation from JSON body.
    // This matches the test request you're sending in VehicleRoutingTest.http.
    post("/reservation") {
        val principal = call.principal<User>()
            ?: return@post call.respond(HttpStatusCode.Unauthorized, "Missing or invalid JWT token")

        val req = call.receive<CreateReservationRequest>()

        // Trust the token, not the request body, to determine the user.
        val authenticatedUserId = principal.id

        // Optional sanity checks
        if (req.vehicleId <= 0) {
            return@post call.respond(HttpStatusCode.BadRequest, "Invalid vehicleId")
        }
        if (req.endAt <= req.startAt) {
            return@post call.respond(HttpStatusCode.BadRequest, "endAt must be after startAt")
        }

        // Validate vehicle exists so we can give a clean 400 instead of an FK 500.
        val vehicleExists = VehicleDao().findById(req.vehicleId) != null
        if (!vehicleExists) {
            return@post call.respond(HttpStatusCode.BadRequest, "Vehicle '${req.vehicleId}' not found")
        }

        // "Already exists" in this codebase means: a row exists for this vehicle (see ReservationsDao.reserveCar).
        val existing = reservationsDao.findAll().firstOrNull { it.vehicleId == req.vehicleId }
        if (existing != null) {
            return@post call.respond(
                HttpStatusCode.Conflict,
                "Reservation already exists: ${existing.id}"
            )
        }

        reservationsDao.reserveCar(
            carId = req.vehicleId,
            userId = authenticatedUserId,
            startTime = req.startAt,
            endTime = req.endAt
        )

        val created = reservationsDao.findAll().firstOrNull { it.vehicleId == req.vehicleId }
        call.respond(HttpStatusCode.Created, created ?: "Reservation created")
    }

    post("/reservation/{id}/{userId}/{startTime}/{endTime}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "Invalid or missing reservation ID"
            )
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "Invalid or missing user ID"
            )

        val startTime: LocalDateTime = LocalDateTime.parse(call.parameters["startTime"].toString())
        val endTime: LocalDateTime = LocalDateTime.parse(call.parameters["endTime"].toString())

        ReservationsDao().reserveCar(id, userId, startTime, endTime)

        call.respond(
            HttpStatusCode.OK,
            "Reservation $id updated with begin time $startTime and end time $endTime"
        )
    }

    post("/reservation/cancel/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "Invalid or missing reservation ID"
            )

        ReservationsDao().cancelReservation(id)

        call.respond(
            HttpStatusCode.OK,
            "Reservation $id has been canceled"
        )
    }

    get("/reservation/myhistory/{userId}") {
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Invalid or missing user ID"
            )

        val reservations: List<Reservations> = reservationsDao.getVehicleReservations(
            userId,
            listOf(
                ReservationStatus.CANCELLED,
                ReservationStatus.COMPLETED
            )
        )

        call.respond(
            HttpStatusCode.OK,
            reservations
        )
    }

    get("/reservation/myrentals/{userId}") {
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Invalid or missing user ID"
            )

        val reservations: List<Reservations> = reservationsDao.getVehicleReservations(
            userId,
            listOf(
                ReservationStatus.PENDING,
                ReservationStatus.CONFIRMED
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = reservations
        )
    }
}

@Serializable
data class CreateReservationRequest(
    val id: Long? = null,
    // userId is accepted for backward compatibility with existing .http files,
    // but it is ignored in favor of the authenticated userId from the JWT token.
    val userId: Long? = null,
    val vehicleId: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val status: ReservationStatus = ReservationStatus.PENDING,
    val totalAmount: Double? = null,
)