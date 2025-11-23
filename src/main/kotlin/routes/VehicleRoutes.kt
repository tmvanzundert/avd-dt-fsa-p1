package com.example.routes

import com.example.models.Vehicle
import com.example.models.VehicleDao
import com.example.models.VehicleStatus
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.datetime.LocalDateTime
import kotlin.reflect.KClass

data class TCO (
    val vehicleId: Long,
    val purchasePrice: Double,
    val energyConsumption: Int,
    val energyPrice: Double,
    val maintenance: Double,
    val insurance: Double,
    val tax: Double,
    val depreciateInYears: Int
)

class VehicleRoute(entityClass: KClass<Vehicle>, override val dao: VehicleDao) : ModelRoute<VehicleDao, Vehicle>("vehicle", entityClass)

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

    post("/vehicle/calculateTCO") {
        val callToJson = try {
            call.receive<TCO>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid JSON body")
            return@post
        }

        val calculate = try {
            vehicleDao.calculateYearlyTCO(
                callToJson.vehicleId,
                callToJson.purchasePrice,
                callToJson.energyConsumption,
                callToJson.energyPrice,
                callToJson.maintenance,
                callToJson.insurance,
                callToJson.tax,
                callToJson.depreciateInYears
            )
        }
        catch (e: Exception) {
            call.respond(HttpStatusCode.NotFound, e)
        }

        call.respond(HttpStatusCode.OK, calculate)
    }

    post("/vehicle/consumptionExpenses/{id}") {
        val id: Long = (call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "Invalid or missing vehicle ID"
            ))

        try {
            val expenses = vehicleDao.consumptionExpenses(id)
            call.respond(HttpStatusCode.OK, mapOf("consumptionExpenses" to expenses))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Error calculating consumption expenses")
        }
    }

    post("/vehicle/calculateYearlyKilometers/{id}") {
        val id: Long = (call.parameters["id"]?.toLongOrNull()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "Invalid or missing vehicle ID"
            ))

        try {
            vehicleDao.calculateYearlyKilometers(id)
            call.respond(
                HttpStatusCode.OK,
                "Yearly kilometers for vehicle $id calculated successfully"
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.NotFound, e.message ?: "Error calculating yearly kilometers")
        }

    }

}