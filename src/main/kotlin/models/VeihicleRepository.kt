package com.example.models

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

interface VehicleRepository: CrudRepository<Vehicle, Long> {
    suspend fun isAvailable(vehicleId: Long): Boolean
}

abstract class VehicleDao: VehicleRepository {
    
    override suspend fun findAll(): List<Vehicle> {
        var vehicles: List<Vehicle> = emptyList()
        transaction {
            vehicles = VehicleTable.selectAll().map {
                Vehicle(
                    id = it[VehicleTable.id],
                    make = it[VehicleTable.make],
                    model = it[VehicleTable.model],
                    year = it[VehicleTable.year],
                    category = it[VehicleTable.category],
                    seats = it[VehicleTable.seats],
                    range = it[VehicleTable.range],
                    odometer = it[VehicleTable.odometer],
                    licensePlate = it[VehicleTable.licensePlate],
                    status = it[VehicleTable.status],
                    location = it[VehicleTable.location],
                    hourlyRate = it[VehicleTable.hourlyRate],
                    customRate = it[VehicleTable.customRate]
                )
            }
        }
        
        return vehicles
    }
    
    override suspend fun findById(id: Long): Vehicle? {
        var vehicle: Vehicle? = null
        transaction {
            val result = VehicleTable.select ( VehicleTable.id eq id ).singleOrNull()
            if (result != null) {
                vehicle = Vehicle(
                    id = result[VehicleTable.id],
                    make = result[VehicleTable.make],
                    model = result[VehicleTable.model],
                    year = result[VehicleTable.year],
                    category = result[VehicleTable.category],
                    seats = result[VehicleTable.seats],
                    range = result[VehicleTable.range],
                    odometer = result[VehicleTable.odometer],
                    licensePlate = result[VehicleTable.licensePlate],
                    status = result[VehicleTable.status],
                    location = result[VehicleTable.location],
                    hourlyRate = result[VehicleTable.hourlyRate],
                    customRate = result[VehicleTable.customRate]
                )
            }
        }
        
        return vehicle?: throw Exception("Vehicle not found")
    }
    
    override suspend fun create(entity: Vehicle) {
        var newVehicle: InsertStatement<Number>? = null
        transaction {
            newVehicle = VehicleTable.insert {
                it[make] = entity.make
                it[model] = entity.model
                it[year] = entity.year
                it[category] = entity.category
                it[seats] = entity.seats
                it[range] = entity.range
                it[odometer] = entity.odometer
                it[licensePlate] = entity.licensePlate
                it[status] = entity.status
                it[location] = entity.location
                it[hourlyRate] = entity.hourlyRate
                it[customRate] = entity.customRate
            }
        }

        if (newVehicle == null) {
            throw Exception("Failed to create vehicle")
        }
    }
    
    override suspend fun update(entity: Vehicle) {
        val vehicleId = findById(entity.id)?.id ?: throw Exception("Vehicle not found")
        
        transaction {
            VehicleTable.update({ VehicleTable.id eq vehicleId }) {
                it[make] = entity.make
                it[model] = entity.model
                it[year] = entity.year
                it[category] = entity.category
                it[seats] = entity.seats
                it[range] = entity.range
                it[odometer] = entity.odometer
                it[licensePlate] = entity.licensePlate
                it[status] = entity.status
                it[location] = entity.location
                it[hourlyRate] = entity.hourlyRate
                it[customRate] = entity.customRate
            }
        }
    }
    
    override suspend fun delete(id: Long) {
        val vehicleId = findById(id) ?: throw Exception("Vehicle not found")

        var rowsDeleted = 0
        transaction {
            rowsDeleted = VehicleTable.deleteWhere { VehicleTable.id eq vehicleId.id }
        }
        
        if (rowsDeleted == 0) {
            throw Exception("Failed to delete vehicle")
        }
    }
    
    override suspend fun isAvailable(vehicleId: Long): Boolean {
        val vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")
        return vehicle.status == VehicleStatus.AVAILABLE
    }
    
}