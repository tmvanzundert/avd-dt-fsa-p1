package com.example.models

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

interface VehicleRepository: CrudRepository<Vehicle, Long> {
    fun isAvailable(vehicleId: Long): Boolean
}

class VehicleDao: VehicleRepository {
    
    override fun findAll(): List<Vehicle> {
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
                    beginOdometer = it[VehicleTable.beginOdometer],
                    endOdometer = it[VehicleTable.endOdometer],
                    licensePlate = it[VehicleTable.licensePlate],
                    status = it[VehicleTable.status],
                    location = it[VehicleTable.location],
                    kilometerRate = it[VehicleTable.kilometerRate],
                    photoPath = it[VehicleTable.photoPath]
                )
            }
        }
        
        return vehicles
    }
    
    override fun findById(id: Long): Vehicle? {
        val users = findAll()
        return users.find { it.id == id }
    }
    
    override fun create(item: Vehicle) {
        // Check if user already exists
        findById(item.id)?.id ?.let {
            throw Exception("User ${item.make} already exists")
        }

        transaction {
            VehicleTable.insert {
                it[id] = item.id
                it[make] = item.make
                it[model] = item.model
                it[year] = item.year
                it[category] = item.category
                it[seats] = item.seats
                it[range] = item.range
                it[beginOdometer] = item.beginOdometer
                it[endOdometer] = item.endOdometer
                it[licensePlate] = item.licensePlate
                it[status] = item.status
                it[location] = item.location
                it[kilometerRate] = item.kilometerRate
                it[photoPath] = item.photoPath
            }
        }
    }
    
    override fun update(item: Vehicle) {
        val vehicleId = findById(item.id) ?: throw Exception("Vehicle not found")
        
        transaction {
            VehicleTable.update({ VehicleTable.id eq vehicleId.id }) {
                it[make] = item.make.ifEmpty { vehicleId.make }
                it[model] = item.model.ifEmpty { vehicleId.model }
                it[year] = if (item.year == 0) vehicleId.year else item.year
                it[category] = item.category.ifEmpty { vehicleId.category }
                it[seats] = if (item.seats == 0) vehicleId.seats else item.seats
                it[range] = if (item.range == 0.0) vehicleId.range else item.range
                it[beginOdometer] = if (item.beginOdometer == 0.0) vehicleId.beginOdometer else item.beginOdometer
                it[endOdometer] = if (item.endOdometer == 0.0) vehicleId.endOdometer else item.endOdometer
                it[licensePlate] = item.licensePlate.ifEmpty { vehicleId.licensePlate }
                it[status] = if (item.status == VehicleStatus.NULL) vehicleId.status else item.status
                it[location] = item.location.ifEmpty { vehicleId.location }
                it[kilometerRate] = if (item.kilometerRate == 0.0) vehicleId.kilometerRate else item.kilometerRate
                it[photoPath] = item.photoPath.ifEmpty { vehicleId.photoPath }
            }
        }
    }
    
    override fun delete(id: Long) {
        val vehicleId = findById(id) ?: throw Exception("Vehicle not found")

        var rowsDeleted = 0
        transaction {
            rowsDeleted = VehicleTable.deleteWhere { VehicleTable.id eq vehicleId.id }
        }
        
        if (rowsDeleted == 0) {
            throw Exception("Failed to delete vehicle")
        }
    }
    
    override fun isAvailable(vehicleId: Long): Boolean {
        val vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")
        return vehicle.status == VehicleStatus.AVAILABLE
    }
    
}