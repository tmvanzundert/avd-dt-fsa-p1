package com.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = false
                explicitNulls = false
                serializersModule = SerializersModule {
                    // Ktor doesn't ship a kotlinx-datetime module by default.
                    // LocalDateTime uses ISO-8601 (e.g. "2026-01-20T11:08").
                    contextual(LocalDateTime::class, LocalDateTime.serializer())
                }
            }
        )
    }

    // Example route to test JSON serialization, can be removed later
    routing {
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}