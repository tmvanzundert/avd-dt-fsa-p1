package com.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        // HTTP GET route for the root path ("/"), responds with "Hello World!" for now.
        // TODO: Replace with actual content later, usage and route endpoints.
        get("/") {
            call.respondText("Hello World!")
        }
        // HTTP GET route for the tail card pattern, this acts as a catch-all for any undefined routes.
        // It redirects to the root path ("/").
        get("{...}") {
            call.respondRedirect("/", permanent = false)
        }
    }
}
