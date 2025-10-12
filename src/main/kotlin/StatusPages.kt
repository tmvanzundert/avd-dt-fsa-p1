package com.example

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.http.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Handle 400 Bad Request errors
        status(HttpStatusCode.BadRequest) { call, status ->
            call.respondText("400: Bad Request", status = status)
        }
        // Handle 401 Unauthorized errors
        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respondText("401: Unauthorized", status = status)
        }
        // Handle 403 Forbidden errors
        status(HttpStatusCode.Forbidden) { call, status ->
            call.respondText("403: Forbidden", status = status)
        }
        // Handle 404 Not Found errors
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondText("404: Resource Not Found", status = status)
        }
        // Handle 500 Internal Server Error
        exception<Throwable> { call, cause ->
            call.respondText("500: Internal Server Error\n${cause.localizedMessage}", status = HttpStatusCode.InternalServerError)
        }
    }
}