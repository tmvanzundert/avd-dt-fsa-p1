package com.example.routes

import com.example.models.Location
import com.example.models.LocationDao
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.reflect.KClass

class LocationRoute(
    entityClass: KClass<Location>,
    override val dao: LocationDao
) : ModelRoute<LocationDao, Location>("location", entityClass)

fun Route.locationRoutes(locationDao: LocationDao) {
    val locationRoute = LocationRoute(Location::class, locationDao)

    locationRoute.apply {
        list()
        getById()
        create()
        update()
        delete()
    }

    get("/location/search") {
        val name = call.request.queryParameters["name"]
        val addressFragment = call.request.queryParameters["addressFragment"]

        if (name == null && addressFragment == null) {
            return@get call.respondText(
                text = "Provide at least one search parameter: 'name' or 'addressFragment'",
                status = HttpStatusCode.BadRequest
            )
        }

        val results = when {
            name != null && addressFragment == null -> {
                locationDao.findByName(name)?.let { listOf(it) } ?: emptyList()
            }
            name == null && addressFragment != null -> {
                locationDao.findByAddressFragment(addressFragment)
            }
            else -> {
                val byName = locationDao.findByName(name!!)
                val byAddress = locationDao.findByAddressFragment(addressFragment!!)
                if (byName == null) {
                    emptyList()
                } else {
                    byAddress.filter { it.id == byName.id }
                }
            }
        }

        if (results.isEmpty()) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(results)
        }
    }
}
