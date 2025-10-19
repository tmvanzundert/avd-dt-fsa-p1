package com.example.models

import org.jetbrains.exposed.v1.core.Table

object LocationTable: Table("Location") {
    val id = varchar("id", 50).primaryKey()
    val name = varchar("name", 255)
    val address = varchar("address", 500)
}

data class Location(
    val id: String,
    val name: String,
    val address: String,
)