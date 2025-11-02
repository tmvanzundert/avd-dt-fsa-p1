package com.example

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain


fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val jwtConfig = jwtConfig()
    configureSerialization()
    configureJWTAuthentication(jwtConfig)
    configureRouting(jwtConfig)
    configureStatusPages()
    configureDatabase()
}

// Create the JWT authentication config to pass to the routing and authentication
fun Application.jwtConfig(): JWTConfig {
    return JWTConfig(
        secret = environment.config.property("ktor.jwt.secret").getString(),
        issuer = environment.config.property("ktor.jwt.issuer").getString(),
        audience = environment.config.property("ktor.jwt.audience").getString(),
        realm = environment.config.property("ktor.jwt.realm").getString(),
        tokenExpiry = environment.config.property("ktor.jwt.tokenExpiry").getString().toLong()
    )
}