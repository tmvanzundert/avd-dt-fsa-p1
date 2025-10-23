package com.example

import com.example.models.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.v1.jdbc.*

fun main() {
    embeddedServer(Netty, port = 8085, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
    configureStatusPages()
    configureDatabase()
    val user: User = User(
        id = 1L,
        name = "John Doe",
        role = Role.USER,
        phone = "123-456-7890",
        password = "password123",
        email = "",
        driverLicenseNumber = "D1234567"
    )
    UserDao().create(user)
}

fun configureDatabase() {
    try {
        Database.connect(
            "jdbc:mariadb://localhost:3306/plugandplay",
            user = "dbuser",
            password = "LkC9STj5n6bztQ"
        )
    }
    catch (e: Exception) {
        throw Exception("Could not connect to the database. Error details: '${e.message}'")
    }
}