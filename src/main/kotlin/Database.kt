package com.example

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureDatabase() {
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