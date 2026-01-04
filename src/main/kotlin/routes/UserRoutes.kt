package com.example.routes

import com.example.models.User
import com.example.models.UserDao
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

class UserRoute(entityClass: KClass<User>, override val dao: UserDao) : ModelRoute<UserDao, User>("user", entityClass) {

}

@Serializable
data class ResetPasswordRequest(
    val username: String,
    /**
     * New password hash (NOT plaintext).
     * The client is responsible for hashing before calling this endpoint.
     */
    val passwordHash: String,
)

fun Route.userRoutes(userDao: UserDao) {
    val userRoute = UserRoute(User::class, userDao)

    userRoute.apply {
        list()
        getById()
        create()
        update()
        delete()
    }

    post("/users/resetpassword") {
        val request = try {
            call.receive<ResetPasswordRequest>()
        } catch (_: Exception) {
            return@post call.respondText(
                "Invalid request body. Expected JSON with 'username' and 'passwordHash'.",
                status = HttpStatusCode.BadRequest
            )
        }

        val username = request.username.trim()
        val passwordHash = request.passwordHash.trim()

        if (username.isBlank()) {
            return@post call.respondText("Missing username", status = HttpStatusCode.BadRequest)
        }
        if (passwordHash.isBlank()) {
            return@post call.respondText("Missing passwordHash", status = HttpStatusCode.BadRequest)
        }

        // Small safety check to reduce accidental plaintext storage.
        // If you're not using bcrypt, remove/adjust this validation.
        val looksLikeBcrypt = passwordHash.startsWith("a$") || passwordHash.startsWith("b$") || passwordHash.startsWith("y$")
        if (!looksLikeBcrypt) {
            return@post call.respondText(
                "passwordHash does not look like a bcrypt hash",
                status = HttpStatusCode.BadRequest
            )
        }

        val user = userDao.findByUsername(username) ?: return@post call.respondText(
            "User with username='$username' not found",
            status = HttpStatusCode.NotFound
        )

        // Store the hash as-is.
        userDao.updateProperty(user.id, "password", passwordHash)

        call.respondText("Password for user '$username' has been reset")
    }
}