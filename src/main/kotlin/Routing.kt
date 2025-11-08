package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import com.example.models.*
import com.example.routes.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlin.reflect.full.memberProperties

// Store all the required information when signing up
@Serializable
data class SignupRequest (
    val firstName: String,
    val lastName: String,
    val username: String,
    val address: String,
    val email: String,
    val password: String
)

// Easily convert the SignupRequest to a User object
fun SignupRequest.toUser(userDao: UserDao): User {
    return User(
        firstName = this.firstName,
        lastName = this.lastName,
        username = this.username,
        address = this.address,
        email = this.email,
        password = userDao.hashPassword(this.password)
    )
}

// Store the username and password when logging in
@Serializable
data class LoginRequest(val username: String, val password: String)

fun Application.configureRouting(jwtConfig: JWTConfig) {
    // Create the DAO objects for use in the routing
    val userDao = UserDao()
    val vehicleDao = VehicleDao()
    val reservationsDao = ReservationsDao()

    routing {

        // If a user wants to sign up, create the user in the database
        post("/signup") {

            // Set the User from the request and throw error if not all fields are filled in
            var user: User
            try {
                // Receive username and password
                val requestData: SignupRequest = call.receive<SignupRequest>()
                user = requestData.toUser(userDao)
            }
            catch (e: Exception) {
                val allowedProperties: List<String> = SignupRequest::class.memberProperties.map { it.name }
                return@post call.respondText("Failed to create the new user. Make sure to at least use the properties in $allowedProperties. Error details: $e")
            }

            // Check if username exists
            if (userDao.findByUsername(user.username) != null) {
                return@post call.respondText("Username '${user.username}' already exists")
            }

            // Create the user with the data from the request
            try {
                userDao.create(user)
                // userDao.create((requestData))
            } catch (e: Exception) {
                call.respondText("$e")
            }

            // Return the response that the user has been created
            return@post call.respond(mapOf("response" to "User ${user.username} has been created"))
        }

        post("/login") {
            // Receive the username and password
            val requestData = call.receive<LoginRequest>()
            val username = requestData.username
            val password = requestData.password

            // Find the user in the database and create a password object
            val findUser = userDao.findByUsername(username)
            val passwordObj = Password(
                hash = findUser?.password ?: return@post call.respondText("Incorrect credentials"),
                plainText = password,
            )

            // Check if the password is correct
            if (!userDao.checkPassword(passwordObj)) {
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
            reservationsRoutes(userDao, reservationsDao)
        }
    }
}