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

    // Simple search: return a location by exact (or case-insensitive) name
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

    // Proximity/“vehicles near location name” search
//    get("/location/search/proximity") {
//        val request = call.receive<LocationSearchRequest>()
//        val name = request.name ?: return@get call.respondText(
//            text = "Please provide a 'name' in the request body",
//            status = HttpStatusCode.BadRequest
//        )
//        // 1. Find locations whose name contains the given fragment
//        val matchingLocations = locationDao.findAll()
//            .filter { it.name.contains(name, ignoreCase = true) }
//        if (matchingLocations.isEmpty()) {
//            println("No locations found matching name fragment: $name")
//            return@get call.respond(HttpStatusCode.NoContent)
//        }
//        // 2. Collect their IDs
//        val locationIds = matchingLocations.map { it.id }.toSet()
//        // 3. Get all vehicles and filter those whose locationId is in these IDs
//        val allVehicles = vehicleDao.findAll()
//        // Adjust this to your actual Vehicle model (locationId / location_id)
//        val vehicleSet = mutableSetOf<Long>()
//
//
//        val vehiclesAtLocations = allVehicles.filter { vehicle ->
//            locationIds.filter {
//                it.contains(vehicle.location)
//            }
//            /*vehicle.location.equals(locationIds)*/
//        }
//
//        /*val vehiclesAtLocations = allVehicles.filter { vehicle ->
//            locationIds.contains(vehicle.location)
//        }*/
//
//        // 4. Respond with vehicles or NoContent
//        if (vehiclesAtLocations.isEmpty()) {
//            call.respond(HttpStatusCode.NoContent)
//        } else {
//            call.respond(vehiclesAtLocations)
//        }

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

            val vehiclesAtLocations = vehicleDao.findAll()
                .filter { vehicle -> vehicle.location.toString() in locationIds }

            if (vehiclesAtLocations.isEmpty()) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(vehiclesAtLocations)
            }
        }

    }
