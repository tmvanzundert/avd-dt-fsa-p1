// NotificationRoutes.kt
package com.example.routes

import com.example.models.UserDao
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    val message: String,
    val userName: String
)

fun Route.notificationRoutes(
    userDao: UserDao,
) {
    post("/notification") {
        val req = call.receive<NotificationRequest>()

        val user = userDao.findByUsername(req.userName)
            ?: return@post call.respond(HttpStatusCode.BadRequest, "User not found")

        // TODO later: save notification with notificationDao

        call.respond(HttpStatusCode.OK, "Notification sent to ${user.username}")
    }
}