package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import com.example.models.*
import com.example.routes.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.*

fun Application.configureRouting(jwtConfig: JWTConfig) {
    val userDao = UserDao()
    val vehicleDao = VehicleDao()


    routing {

        post("signup") {
            val requestData = call.receive<AuthRequest>()
            val username = userDao.findByUsername(requestData.username)
            val user = User(
                firstName = "",
                lastName = "",
                username = requestData.username,
                role = Role.USER,
                phone = "",
                password = UserDao().hashPassword(requestData.password),
                email = "",
                driverLicenseNumber = ""
            )
            if (username != null) {
                return@post call.respondText("Username already exists")
            } else {
                try {
                    userDao.create(user)
                } catch (e: Exception) {
                    call.respondText("$e")
                }
                val token = generateToken(config = jwtConfig, username = requestData.username)// hashMapOf?
                return@post call.respond(mapOf("token" to token))
            }
        }

        post("login") {
            val requestData = call.receive<AuthRequest>()

            val username = userDao.findByUsername(requestData.username)
            val password = Password(
                hash = username?.password ?: "",
                plainText = requestData.password,
            )

            if (username == null || !userDao.checkPassword(password)) {
                return@post call.respondText("Incorrect credentials")
            } else {
                val token = generateToken(config = jwtConfig, username = requestData.username)
//                return@post call.respondText("welcome ${username.username}, password: ${password.plainText}, token: ${token}")
                return@post call.respond(mapOf("token" to token))
            }
        }

        authenticate("jwt-auth") {
            imageRoutes()
            userRoutes(userDao)
            vehicleRoutes(vehicleDao)
        }
    }
}