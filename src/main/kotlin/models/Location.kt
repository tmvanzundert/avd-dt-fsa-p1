package com.example.models

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import kotlinx.serialization.Serializable

object LocationTable: Table("locations") {
    val id = varchar("id", 50)
    val name = varchar("name", 255)
    val address = varchar("address", 500)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Serializable
data class Location(
    val id: String,
    val name: String,
    val address: String,
)