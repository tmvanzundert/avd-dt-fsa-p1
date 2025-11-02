package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import com.example.models.*
import com.example.routes.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

// Store the username and password when logging in
@Serializable
data class AuthRequest(val username: String, val password: String)

fun Application.configureRouting(jwtConfig: JWTConfig) {
    // Create the DAO objects for use in the routing
    val userDao = UserDao()
    val vehicleDao = VehicleDao()

    routing {

        // If a user wants to sign up, create the user in the database
        post("signup") {
            // Receive username and password
            val requestData = call.receive<AuthRequest>()
            // val requestData: User = call.receive<User>()
            val username = requestData.username
            val password = requestData.password

            // Check if username exists
            if (userDao.findByUsername(username) != null) {
                return@post call.respondText("Username '$username' already exists")
            }

            val user = User(
                firstName = "",
                lastName = "",
                username = username,
                address = "",
                role = Role.USER,
                phone = "",
                password = UserDao().hashPassword(password),
                email = "",
                driverLicenseNumber = ""
            )

            // Create the user with the data from the request
            try {
                userDao.create(user)
                // userDao.create((requestData))
            } catch (e: Exception) {
                call.respondText("$e")
            }

            // Return the response that the user has been created
            return@post call.respond(mapOf("response" to "User $username has been created"))
        }

        post("login") {
            // Receive the username and password
            val requestData = call.receive<AuthRequest>()
            val username = requestData.username
            val password = requestData.password

            // Find the user in the database and create a password object
            val findUser = userDao.findByUsername(username)
            val passwordObj = Password(
                hash = findUser?.password ?: "",
                plainText = password,
            )

            // Check if username exists
            if (findUser == null || !userDao.checkPassword(passwordObj)) {
                return@post call.respondText("Incorrect credentials")
            }

            // Generate the JWT token and return to the request
            val token = generateToken(config = jwtConfig, username = username)
            return@post call.respond(mapOf("token" to token))
        }

        // Import the other routes that are written in other files for readability
        authenticate("jwt-auth") {
            imageRoutes()
            userRoutes(userDao)
            vehicleRoutes(vehicleDao)
        }
    }
}