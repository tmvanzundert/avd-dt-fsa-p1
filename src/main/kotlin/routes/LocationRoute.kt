package com.example.routes

import com.example.models.Location
import com.example.models.LocationDao
import io.ktor.http.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
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

    @Serializable
    data class LocationSearchRequest(val name: String?)

    get("/location/search") {
        val request = call.receive<LocationSearchRequest>()
        val name = request.name ?: return@get call.respondText(
            text = "Provide at least one search parameter: 'name' or 'addressFragment'",
            status = HttpStatusCode.BadRequest
        )

        val location = locationDao.findByName(name)

        if (location == null) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(location)
        }
    }
}
