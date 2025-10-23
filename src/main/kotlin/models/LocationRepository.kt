package com.example.models

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

interface LocationRepository: CrudRepository<Location, String> {

}

class LocationDao: LocationRepository {

    override fun findAll(): List<Location> {
        var locations = listOf<Location>()
        transaction {
            locations = LocationTable.selectAll().map {
                Location(
                    id = it[LocationTable.id],
                    name = it[LocationTable.name],
                    address = it[LocationTable.address]
                )
            }
        }

        return locations
    }

    override fun findById(id: String): Location? {
        val users = findAll()
        return users.find { it.id == id }
    }

    override fun create(item: Location) {
        // Check if user already exists
        findById(item.id)?.id ?.let {
            throw Exception("User ${item.name} already exists")
        }

        transaction {
            LocationTable.insert {
                it[id] = item.id
                it[name] = item.name
                it[address] = item.address
            }
        }
    }

    override fun update(item: Location) {
        // Throw error if id does not exist in the database
        findById(item.id) ?: throw Exception("Location not found")

        transaction {
            LocationTable.update({ LocationTable.id eq item.id }) {
                it[name] = item.name
                it[address] = item.address
            }
        }
    }

    override fun delete(id: String) {
        // Throw error if id does not exist in the database
        findById(id) ?: throw Exception("Location not found")

        transaction {
            LocationTable.deleteWhere { LocationTable.id eq id }
        }
    }

}