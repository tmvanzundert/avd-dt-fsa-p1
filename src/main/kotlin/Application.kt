package com.example

import com.example.plugins.JWTConfig
import com.example.plugins.configureJWTAuthentication
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    val jwt = environment.config.config("ktor.jwt")
    val realm = jwt.property("realm").getString()
    val secret = jwt.property("secret").getString()
    val issuer = jwt.property("issuer").getString()
    val audience = jwt.property("audience").getString()
    val tokenExpiry = jwt.property("tokenExpiry").getString().toLong()

    val config = JWTConfig(
        realm = realm,
        secret = secret,
        issuer = issuer,
        audience = audience,
        tokenExpiry = tokenExpiry
    )

    configureSerialization()
    configureJWTAuthentication(config)
    configureRouting(config)

}
