package com.example.models

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

interface VehicleRepository: CrudRepository<Vehicle, Long> {
    fun isAvailable(vehicleId: Long): Boolean
    fun calculateYearlyTCO(vehicleId: Long, purchasePrice: Double, energyConsumption: Int, energyPrice: Double, maintenance: Double, insurance: Double, tax: Double, depreciateInYears: Int = 15): Double
    fun consumptionExpenses(vehicleId: Long): Double
    fun calculateYearlyKilometers(vehicleId: Long)
}

class VehicleDao: CrudDAO<Vehicle, Long, VehicleTable>(VehicleTable), VehicleRepository {

    // Check if a vehicle is available
    override fun isAvailable(vehicleId: Long): Boolean {
        val vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")
        return vehicle.status == VehicleStatus.AVAILABLE
    }

    // Calculate the yearly Total Cost of Ownership
    override fun calculateYearlyTCO(vehicleId: Long, purchasePrice: Double, energyConsumption: Int, energyPrice: Double, maintenance: Double, insurance: Double, tax: Double, depreciateInYears: Int): Double {
        val vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")

        val depreciation = purchasePrice / depreciateInYears
        val energyCost = vehicle.totalYearlyUsageKilometers / 100 * energyConsumption * energyPrice

        return depreciation + maintenance + insurance + tax + energyCost
    }

    override fun consumptionExpenses(vehicleId: Long): Double {
        TODO("Not yet implemented")
    }

    override fun calculateYearlyKilometers(vehicleId: Long) {
        TODO("Not yet implemented")
    }

    // Calculate how much the vehicle consumes in expenses per kilometer
    /*override fun consumptionExpenses(vehicleId: Long): Double {
        val vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")

        val rentalContracts: RentalContract = RentalContract().findAll()
        val rentalContract: RentalContract = rentalContracts.find{ rentalContracts.reservationId == vehicle.id } ?: throw Exception("Rental contract not found")

        val ratePlans: RatePlan = RatePlan().findAll()
        val ratePlan: RatePlan = ratePlans.find{ ratePlans.reservationId == rentalContract.id } ?: throw Exception("Rate plan not found")

        return (rentalContract.endOdometer - rentalContract.beginOdometer) / ratePlan.pricePerKm
    }

    // Calculate how many kilometers the vehicle has driven in a year
    override fun calculateYearlyKilometers(vehicleId: Long) {
        val vehicle: Vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")
        val rentalContracts: RentalContract = RentalContract().findAll()
        val rentalContract: RentalContract = rentalContracts.find{ rentalContracts.reservationId == vehicle.id } ?: throw Exception("Rental contract not found")

        val yearlyUsage: Long = rentalContract.endOdometer - rentalContract.beginOdometer
        updateProperty(vehicle.id, "totalYearlyUsageKilometers", yearlyUsage)
    }*/

    // Map all the database columns to the Vehicle data class
    override fun getEntity(row: ResultRow): Vehicle {
        return Vehicle(
            id = row[VehicleTable.id],
            make = row[VehicleTable.make],
            model = row[VehicleTable.model],
            year = row[VehicleTable.year],
            category = row[VehicleTable.category],
            seats = row[VehicleTable.seats],
            range = row[VehicleTable.range],
            licensePlate = row[VehicleTable.licensePlate],
            location = row[VehicleTable.location],
            ownerId = row[VehicleTable.ownerId],
            photoPath = row[VehicleTable.photoPath],
            totalYearlyUsageKilometers = row[VehicleTable.totalYearlyUsageKilometers]
        )
    }

    // Prepare a statement to create or update an entity in the database
    override fun createEntity(entity: Vehicle, statement: UpdateBuilder<Int>) {
        statement[VehicleTable.id] = entity.id
        statement[VehicleTable.make] = entity.make
        statement[VehicleTable.model] = entity.model
        statement[VehicleTable.year] = entity.year
        statement[VehicleTable.category] = entity.category
        statement[VehicleTable.seats] = entity.seats
        statement[VehicleTable.range] = entity.range
        statement[VehicleTable.licensePlate] = entity.licensePlate
        statement[VehicleTable.location] = entity.location
        statement[VehicleTable.ownerId] = entity.ownerId
        statement[VehicleTable.photoPath] = entity.photoPath
        statement[VehicleTable.totalYearlyUsageKilometers] = entity.totalYearlyUsageKilometers
    }

}