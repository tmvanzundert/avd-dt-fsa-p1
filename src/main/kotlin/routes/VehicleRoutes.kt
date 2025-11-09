package com.example.routes

import com.example.models.Vehicle
import com.example.models.VehicleDao
import com.example.models.VehicleStatus
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
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

}