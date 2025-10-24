package com.example

import com.example.models.User
import com.example.models.UserDao
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userDao = UserDao()

    routing {
        // List all users
        get("/users") {
            call.respond(userDao.findAll())
        }

        // Get user by ID
        get("/users/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@get
            }
            val user = userDao.findById(id)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, "User not found")
            } else {
                call.respond(user)
            }
        }

        // Create user
        post("/users") {
            val user = call.receive<User>()
            try {
                userDao.create(user)
                call.respond(HttpStatusCode.Created, user)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, e.message ?: "User already exists")
            }
        }

        // Update user
        put("/users/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@put
            }
            val user = call.receive<User>()
            if (user.id != id) {
                call.respond(HttpStatusCode.BadRequest, "User ID mismatch")
                return@put
            }
            try {
                userDao.update(user)
                call.respond(HttpStatusCode.OK, user)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, e.message ?: "User not found")
            }
        }

        // Delete user
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@delete
            }
            try {
                userDao.delete(id)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, e.message ?: "User not found")
            }
        }
    }
}