package com.example.routes

import com.example.models.NotificationDao
import com.example.models.UserDao
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
data class NotificationRequest(
    val message: String,
    val userName: String,
    val type: String = "INFO"
)

@OptIn(ExperimentalTime::class)
fun Route.notificationRoutes(
    userDao: UserDao,
    notificationDao: NotificationDao
) {
    post("/notification") {
        val req = call.receive<NotificationRequest>()

        val user = userDao.findByUsername(req.userName)
            ?: return@post call.respond(HttpStatusCode.BadRequest, "User not found")

        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC)

        val notification = notificationDao.createNotification(
            userId = user.id,
            type = req.type,
            message = req.message,
            timestamp = now
        )

        call.respond(
            HttpStatusCode.OK,
            "Notification ${notification.id} sent to ${user.username}"
        )
    }
}
