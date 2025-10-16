package com.example

import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.configureRouting(config: JWTConfig) {

//    TODO: Deze poes moet opmiauwen zodra we een DB hebben.
    val usersDB = mutableMapOf<String,String>()

    routing {

        post("signup") {
            val requestData = call.receive<AuthRequest>()
            if (usersDB.containsKey(requestData.username)){
                call.respondText("User already exists")
            }else{
                usersDB[requestData.username] = requestData.password
                val token = generateToken(config = config, username = requestData.username)

                call.respond(mapOf("token" to token))
            }
        }

        post("login") {
            val requestData = call.receive<AuthRequest>()

            val storedPassword = usersDB[requestData.username]
                ?: return@post call.respondText("User doesn't exist")

            if (storedPassword == requestData.password) {
                val token = generateToken(config = config, username = requestData.username)
                call.respond(mapOf("token" to token))
            }else{
                call.respondText("Invalid credentials")
            }
        }

        authenticate("jwt-auth") {
            get(""){
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                val expiresAt = principal?.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respondText("hello, $username! The token expires after $expiresAt ms.")
            }
        }
    }
}

@Serializable
data class AuthRequest(
    val username: String,
    val password: String
)
