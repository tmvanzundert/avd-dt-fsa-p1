package com.example.routes

import com.example.models.Vehicle
import com.example.models.VehicleDao
import com.example.models.VehicleStatus
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.datetime.LocalDateTime
import kotlin.reflect.KClass

class VehicleRoute(entityClass: KClass<Vehicle>, override val dao: VehicleDao) : ModelRoute<VehicleDao, Vehicle>("vehicle", entityClass) {

}

fun Route.vehicleRoutes(vehicleDao: VehicleDao) {
    val vehicleRoute = VehicleRoute(Vehicle::class, vehicleDao)

    vehicleRoute.apply {
        list()
        getById()
        create()
        update()
        delete()
    }

    get("/vehicle/available") {
        val vehicles = vehicleDao.findAll()
        val available = vehicles.filter { it.status == VehicleStatus.AVAILABLE }
        println("Current vehicles: $vehicles")
        println("Available vehicles: $available")

        if (available.isEmpty()) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(available)
            call.respond(vehicles)
        }
    }

    get("/vehicle/{timeStart}/{timeEnd}") {
        val timeStart: LocalDateTime = LocalDateTime.parse(call.parameters["timeStart"]!!)
        val timeEnd: LocalDateTime = LocalDateTime.parse(call.parameters["timeEnd"]!!)

        val filterVehicles = VehicleDao().findByTimeAvailable(timeStart, timeEnd)
        if (filterVehicles.isEmpty()) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(filterVehicles)
        }
    }

    get("/vehicle/available/{id}/{timeStart}/{timeEnd}") {
        val id: Long = (call.parameters["id"]?.toLongOrNull()
            ?: call.respond(
                HttpStatusCode.BadRequest,
                "Invalid or missing vehicle ID"
            )) as Long

        val timeStart: LocalDateTime = LocalDateTime.parse(call.parameters["timeStart"]!!)
        val timeEnd: LocalDateTime = LocalDateTime.parse(call.parameters["timeEnd"]!!)

        vehicleDao.updateProperty(id, "status", VehicleStatus.AVAILABLE)
        vehicleDao.updateProperty(id, "beginAvailable", timeStart)
        vehicleDao.updateProperty(id, "endAvailable", timeEnd)

        call.respond(
            HttpStatusCode.OK,
            "Vehicle $id is now available"
        )
    }

}