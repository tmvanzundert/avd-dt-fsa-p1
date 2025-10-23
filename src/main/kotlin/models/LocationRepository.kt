package com.example.models

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

interface LocationRepository: CrudRepository<Location, String> {

}

class LocationDao: LocationRepository {

    override suspend fun findAll(): List<Location> {
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

    override suspend fun findById(id: String): Location? {
        var location: Location? = null
        transaction {
            val result = LocationTable.select ( LocationTable.id eq id ).singleOrNull()
            if (result != null) {
                location = Location(
                    id = result[LocationTable.id],
                    name = result[LocationTable.name],
                    address = result[LocationTable.address]
                )
            }
        }

        return location ?: throw Exception("Location not found")
    }

    override suspend fun create(item: Location) {
        // Throw error if id already exists in the database
        findById(item.id) ?: throw Exception("Location already exists")

        transaction {
            LocationTable.insert {
                it[id] = item.id
                it[name] = item.name
                it[address] = item.address
            }
        }
    }

    override suspend fun update(item: Location) {
        // Throw error if id does not exist in the database
        findById(item.id) ?: throw Exception("Location not found")

        transaction {
            LocationTable.update({ LocationTable.id eq item.id }) {
                it[name] = item.name
                it[address] = item.address
            }
        }
    }

    override suspend fun delete(id: String) {
        // Throw error if id does not exist in the database
        findById(id) ?: throw Exception("Location not found")

        transaction {
            LocationTable.deleteWhere { LocationTable.id eq id }
        }
    }

}