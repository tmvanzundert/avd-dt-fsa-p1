package com.example

import com.example.models.*
import com.example.routes.*
import io.ktor.server.application.Application
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userDao = UserDao()
    val vehicleDao = VehicleDao()

    routing {
        userRoutes(userDao)
        vehicleRoutes(vehicleDao)
    }
}