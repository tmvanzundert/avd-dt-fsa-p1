package com.example

import com.example.models.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import kotlinx.serialization.Serializable


fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val jwtConfig = jwtConfig()
    configureSerialization()
    configureJWTAuthentication(jwtConfig)
    configureRouting(jwtConfig)
    configureStatusPages()
    configureDatabase()
}

fun Application.jwtConfig(): JWTConfig {
    return JWTConfig(
        secret = environment.config.property("ktor.jwt.secret").getString(),
        issuer = environment.config.property("ktor.jwt.issuer").getString(),
        audience = environment.config.property("ktor.jwt.audience").getString(),
        realm = environment.config.property("ktor.jwt.realm").getString(),
        tokenExpiry = environment.config.property("ktor.jwt.tokenExpiry").getString().toLong()
    )
}

@Serializable
data class AuthRequest(val username: String, val password: String)