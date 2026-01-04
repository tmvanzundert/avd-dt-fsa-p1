package com.example.routes

import com.example.models.Vehicle
import com.example.models.VehicleDao
import com.example.models.VehicleStatus
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

class VehicleRoute(entityClass: KClass<Vehicle>, override val dao: VehicleDao) : ModelRoute<VehicleDao, Vehicle>("vehicle", entityClass) {

}

@Serializable
data class VehicleLocation(
    val longitude: Double,
    val latitude: Double,
)

@Serializable
data class ChangeVehicleStatusRequest(
    val status: VehicleStatus,
    /** Optional: only meaningful when setting status to AVAILABLE in current domain model. */
    val beginAvailable: LocalDateTime? = null,
    /** Optional: only meaningful when setting status to AVAILABLE in current domain model. */
    val endAvailable: LocalDateTime? = null,
)

fun Route.vehicleRoutes(vehicleDao: VehicleDao) {
    val vehicleRoute = VehicleRoute(Vehicle::class, vehicleDao)

    vehicleRoute.apply {
        list()
        getById()
        create()
        update()
        delete()
    }

    /**
     * Change vehicle status.
     *
     * POST /vehicle/{id}/status
     * Body:
     * { "status": "AVAILABLE", "beginAvailable": "2026-01-04T10:00", "endAvailable": "2026-01-04T18:00" }
     */
    post("/vehicle/{id}/status") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respondText(
                "Invalid or missing vehicle ID",
                status = HttpStatusCode.BadRequest,
            )

        // Ensure vehicle exists before updating.
        vehicleDao.findById(id) ?: return@post call.respondText(
            "Vehicle with id='$id' not found",
            status = HttpStatusCode.NotFound,
        )

        val body = try {
            call.receive<ChangeVehicleStatusRequest>()
        } catch (_: Exception) {
            return@post call.respondText(
                "Invalid request body. Expected JSON with at least 'status'.",
                status = HttpStatusCode.BadRequest,
            )
        }

        // Always update the status.
        vehicleDao.updateProperty(id, "status", body.status)

        // Optionally update availability window when provided.
        // We only persist the fields that are non-null to avoid accidentally wiping values.
        body.beginAvailable?.let { vehicleDao.updateProperty(id, "beginAvailable", it) }
        body.endAvailable?.let { vehicleDao.updateProperty(id, "endAvailable", it) }

        call.respond(
            HttpStatusCode.OK,
            "Vehicle $id status updated to ${body.status}"
        )
    }

    get("/vehicle/available") {
        val vehicles = vehicleDao.findAll()
        val available = vehicles.filter { it.status == VehicleStatus.AVAILABLE }

        if (available.isEmpty()) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(available)
        }
    }

    get("/vehicle/{timeStart}/{timeEnd}") {
        val timeStart: LocalDateTime = LocalDateTime.parse(call.parameters["timeStart"]!!)
        val timeEnd: LocalDateTime = LocalDateTime.parse(call.parameters["timeEnd"]!!)

        val filterVehicles = vehicleDao.findByTimeAvailable(timeStart, timeEnd)
        if (filterVehicles.isEmpty()) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(filterVehicles)
        }
    }

    post("/vehicle/{id}/location") {
        val id = call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respondText(
                "Invalid or missing vehicle ID",
                status = HttpStatusCode.BadRequest,
            )

        // Ensure vehicle exists before updating.
        vehicleDao.findById(id) ?: return@post call.respondText(
            "Vehicle with id='$id' not found",
            status = HttpStatusCode.NotFound,
        )

        val body = try {
            call.receive<VehicleLocation>()
        } catch (_: Exception) {
            return@post call.respondText(
                "Invalid request body. Expected JSON with 'longitude' and 'latitude'.",
                status = HttpStatusCode.BadRequest,
            )
        }

        vehicleDao.setLocation(id, body.longitude, body.latitude)

        call.respond(
            HttpStatusCode.OK,
            "Vehicle $id location updated to (${body.longitude}, ${body.latitude})"
        )
    }

}