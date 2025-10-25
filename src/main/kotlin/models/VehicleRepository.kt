package com.example.models

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

interface VehicleRepository: CrudRepository<Vehicle, Long> {
    fun isAvailable(vehicleId: Long): Boolean
    fun calculateYearlyTCO(vehicleId: Long, purchasePrice: Double, energyConsumption: Int, energyPrice: Double, maintenance: Double, insurance: Double, tax: Double, depreciateInYears: Int = 15): Double
    fun consumptionExpenses(vehicleId: Long): Double
    fun calculateYearlyKilometers(vehicleId: Long): Double
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
                    price = it[VehicleTable.price],
                    photoPath = it[VehicleTable.photoPath],
                    beginReservation = it[VehicleTable.beginReservation],
                    endReservation = it[VehicleTable.endReservation],
                    totalYearlyUsageKilometers = it[VehicleTable.totalYearlyUsageKilometers]
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
                it[price] = item.price
                it[photoPath] = item.photoPath
                it[beginReservation] = item.beginReservation
                it[endReservation] = item.endReservation
                it[totalYearlyUsageKilometers] = item.totalYearlyUsageKilometers
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
                it[price] = if (item.price == 0.0) vehicleId.price else item.price
                it[photoPath] = item.photoPath.ifEmpty { vehicleId.photoPath }
                it[beginReservation] = item.beginReservation
                it[endReservation] = item.endReservation
                it[totalYearlyUsageKilometers] = if (item.totalYearlyUsageKilometers == 0.0) vehicleId.totalYearlyUsageKilometers else item.totalYearlyUsageKilometers
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

    override fun calculateYearlyTCO(vehicleId: Long, purchasePrice: Double, energyConsumption: Int, energyPrice: Double, maintenance: Double, insurance: Double, tax: Double, depreciateInYears: Int): Double {
        val vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")

        val depreciation = purchasePrice / depreciateInYears
        val energyCost = vehicle.totalYearlyUsageKilometers / 100 * energyConsumption * energyPrice

        return depreciation + maintenance + insurance + tax + energyCost
    }

    override fun consumptionExpenses(vehicleId: Long): Double {
        val vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")
        return (vehicle.endOdometer - vehicle.beginOdometer) / vehicle.price
    }

    override fun calculateYearlyKilometers(vehicleId: Long): Double {
        val vehicle: Vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")

        vehicle.totalYearlyUsageKilometers = vehicle.endOdometer - vehicle.beginOdometer
        update(vehicle)
        return vehicle.totalYearlyUsageKilometers
    }
    
}