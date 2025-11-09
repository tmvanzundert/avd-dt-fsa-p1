package com.example.routes

import com.example.models.Location
import com.example.models.LocationDao
import com.example.models.VehicleDao
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

class LocationRoute(
    entityClass: KClass<Location>,
    override val dao: LocationDao
) : ModelRoute<LocationDao, Location>("location", entityClass)

@Serializable
data class LocationSearchRequest(val name: String?)

fun Route.locationRoutes(locationDao: LocationDao, vehicleDao: VehicleDao) {
    val locationRoute = LocationRoute(Location::class, locationDao)

    locationRoute.apply {
        list()
        getById()
        create()
        update()
        delete()
    }

    get("/location/search") {
        val request = call.receive<LocationSearchRequest>()
        val name = request.name ?: return@get call.respondText(
            text = "Please provide a 'name' in the request body",
            status = HttpStatusCode.BadRequest
        )

        val location = locationDao.findByName(name)

        if (location == null) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(location)
        }
    }

    get("/location/search/proximity") {
        val name = call.receive<LocationSearchRequest>().name
            ?: return@get call.respondText(
                text = "Please provide a 'name' in the request body",
                status = HttpStatusCode.BadRequest
            )

        val locationIds = locationDao.findAll()
            .filter { it.name.contains(name, ignoreCase = true) }
            .map { it.id }
            .toSet()
            .also { ids ->
                if (ids.isEmpty()) {
                    return@get call.respond(HttpStatusCode.NoContent)
                }
            }

        val availableVehiclesAtLocations = vehicleDao.findAll()
            .filter { vehicle -> vehicle.location.toString() in locationIds }
            .filter { vehicle -> vehicleDao.isAvailable(vehicle.id) }

        if (availableVehiclesAtLocations.isEmpty()) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(availableVehiclesAtLocations)
        }
    }

}
