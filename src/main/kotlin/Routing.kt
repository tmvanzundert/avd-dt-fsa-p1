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

@Serializable
data class SignupRequest (
    val firstName: String,
    val lastName: String,
    val username: String,
    val address: String,
    val email: String,
    val password: String
)

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

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: Long
)

fun Application.configureRouting(jwtConfig: JWTConfig) {
    val userDao = UserDao()
    val vehicleDao = VehicleDao()
    val reservationsDao = ReservationsDao()
    val paymentsDao = PaymentsDao()

    routing {

        post("/signup") {

            // Set the User from the request and throw error if not all fields are filled in
            val user: User = try {
                // Receive username and password
                val requestData: SignupRequest = call.receive()
                requestData.toUser(userDao)
            } catch (e: Exception) {
                val allowedProperties: List<String> = SignupRequest::class.memberProperties.map { it.name }
                return@post call.respondText(
                    "Failed to create the new user. Make sure to at least use the properties in $allowedProperties. Error details: $e"
                )
            }

            // Check if username exists
            if (userDao.findByUsername(user.username) != null) {
                return@post call.respondText("Username '${user.username}' already exists")
            }

            // Create the user with the data from the request
            try {
                userDao.create(user)
            } catch (e: Exception) {
                return@post call.respondText("$e")
            }

            // Fetch created user so we can return its id
            val createdUser = userDao.findByUsername(user.username)
                ?: return@post call.respondText("Failed to create user")

            // Generate JWT token for the new user and return it
            val token = generateToken(config = jwtConfig, username = createdUser.username, userId = createdUser.id)
            return@post call.respond(AuthResponse(token = token, userId = createdUser.id))
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
            val token = generateToken(config = jwtConfig, username = username, userId = findUser.id)
            return@post call.respond(AuthResponse(token = token, userId = findUser.id))
        }

        authenticate("jwt-auth") {
            imageRoutes()
            userRoutes(userDao)
            vehicleRoutes(vehicleDao)
            reservationsRoutes(reservationsDao, userDao)
            paymentRoutes(paymentsDao)
        }
    }
}