package com.example.models

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

interface LocationRepository: CrudRepository<Location, Long> {
    fun addLocation(location: Location): Location
    fun findByName(name: String): Location?
}

class LocationDao :
    CrudDAO<Location, Long, LocationTable>(LocationTable),
    LocationRepository {

    override fun getEntity(row: ResultRow): Location {
        return Location(
            id = row[LocationTable.id],
            name = row[LocationTable.name],
            address = row[LocationTable.address],
        )
    }

    override fun createEntity(entity: Location, statement: UpdateBuilder<Int>) {
        statement[LocationTable.id] = entity.id
        statement[LocationTable.name] = entity.name
        statement[LocationTable.address] = entity.address
    }

    override fun addLocation(location: Location): Location {
        create(location)
        val inserted = findAll().maxByOrNull { it.id }!!
        return inserted
    }

    override fun findByName(name: String): Location? {
        return findAll().find { it.name.equals(name, ignoreCase = true) }
    }

}