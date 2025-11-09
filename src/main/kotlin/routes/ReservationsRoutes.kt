package com.example.routes

import com.example.models.ReservationsDao
import com.example.models.UserDao
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

fun Route.reservationsRoutes(
    userDao: UserDao,
    reservationsDao: ReservationsDao,

) {
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