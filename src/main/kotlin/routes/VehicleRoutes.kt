package com.example.routes

import com.example.models.Vehicle
import com.example.models.VehicleDao
import io.ktor.server.routing.Route
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
}