package com.example.routes

import com.example.models.Vehicle
import com.example.models.VehicleDao
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.vehicleRoutes(vehicleDao: VehicleDao) {
    // List all vehicles
    get("/vehicles") {
        call.respond(vehicleDao.findAll())
    }

    // Get vehicle by ID
    get("/vehicles/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid vehicle ID")
            return@get
        }
        val vehicle = vehicleDao.findById(id)
        if (vehicle == null) {
            call.respond(HttpStatusCode.NotFound, "Vehicle not found")
        } else {
            call.respond(vehicle)
        }
    }

    // Create vehicle
    post("/vehicles") {
        val vehicle = call.receive<Vehicle>()
        try {
            vehicleDao.create(vehicle)
            call.respond(HttpStatusCode.Created, vehicle)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Conflict, e.message ?: "Vehicle already exists")
        }
    }

    // Update vehicle
    put("/vehicles/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid vehicle ID")
            return@put
        }
        val vehicle = call.receive<Vehicle>()
        if (vehicle.id != id) {
            call.respond(HttpStatusCode.BadRequest, "Vehicle ID mismatch")
            return@put
        }
        try {
            vehicleDao.update(vehicle)
            call.respond(HttpStatusCode.OK, vehicle)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.NotFound, e.message ?: "Vehicle not found")
        }
    }

    // Delete vehicle
    delete("/vehicles/{id}") {
        val id = call.parameters["id"]?.toLongOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid vehicle ID")
            return@delete
        }
        try {
            vehicleDao.delete(id)
            call.respond(HttpStatusCode.NoContent)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.NotFound, e.message ?: "Vehicle not found")
        }
    }
}