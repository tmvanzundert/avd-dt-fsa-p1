package com.example.routes

import com.example.models.ReservationStatus
import com.example.models.Reservations
import com.example.models.ReservationsDao
import com.example.models.User
import com.example.models.UserDao
import com.example.models.VehicleDao
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

class ReservationsRoute(entityClass: KClass<Reservations>, override val dao: ReservationsDao) : ModelRoute<ReservationsDao, Reservations>("user", entityClass) {

}

fun Route.reservationsRoutes(
    reservationsDao: ReservationsDao,
    userDao: UserDao,

) {
    val reservationRoute = ReservationsRoute(Reservations::class, reservationsDao)

    reservationRoute.apply {
        list()
        getById()
        create()
        update()
        delete()
    }

    post("/reservation") {
        val request = call.receive<CreateReservationRequest>()

        val user = userDao.findByUsername(request.userName)
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "User '${request.userName}' not found"
            )

        val responseBody = ReservationResponse(
            vehicleId = request.vehicleId,
            userName = user.username
        )

        call.respond(HttpStatusCode.OK, responseBody)
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

    post("/reservation/myhistory/{userId}") {
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "Invalid or missing user ID"
            )

        val vehicles = reservationsDao.getVehicleReservations(userId, listOf(
            ReservationStatus.CANCELLED,
            ReservationStatus.COMPLETED
        ))

        call.respond(
            HttpStatusCode.OK,
            vehicles
        )
    }

    post("/reservation/myrentals/{userId}") {
        val userId = call.parameters["userId"]?.toLongOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "Invalid or missing user ID"
            )

        val vehicles = reservationsDao.getVehicleReservations(userId, listOf(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED
        ))

        call.respond(
            status = HttpStatusCode.OK,
            message = vehicles
        )
    }
}

@Serializable
data class CreateReservationRequest(
    val vehicleId: Long,
    val userName: String,
)

@Serializable
data class ReservationResponse(
    val vehicleId: Long,
    val userName: String,
)