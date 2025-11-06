package com.example.models

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

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

        val cost = depreciation + maintenance + insurance + tax + energyCost

        updateProperty(vehicleId, "tco", cost)

        return cost
    }

    // Calculate how much the vehicle consumes in expenses per kilometer
    override fun consumptionExpenses(vehicleId: Long): Double {
        val vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")

        val rentalContracts: List<RentalContract> = RentalContractDao().findAll()
        val rentalContract: RentalContract = rentalContracts.find{ it.vehicleId == vehicle.id } ?: throw Exception("Rental contract not found")

        val ratePlans: List<RatePlan> = RatePlanDao().findAll()
        val ratePlan: RatePlan = ratePlans.find{ it.rentalContractId == rentalContract.id } ?: throw Exception("Rate plan not found")

        return (rentalContract.dropoffOdometer - rentalContract.pickupOdometer) / ratePlan.pricePerKm
    }

    // Calculate how many kilometers the vehicle has driven in a year
    override fun calculateYearlyKilometers(vehicleId: Long) {
        val vehicle: Vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")
        val rentalContracts: List<RentalContract> = RentalContractDao().findAll()
        val rentalContract: RentalContract = rentalContracts.find{ it.vehicleId == vehicle.id } ?: throw Exception("Rental contract not found")

        val yearlyUsage: Long = rentalContract.dropoffOdometer - rentalContract.pickupOdometer
        updateProperty(vehicle.id, "totalYearlyUsageKilometers", yearlyUsage)
    }

    // Map all the database columns to the Vehicle data class
    override fun getEntity(row: ResultRow): Vehicle {
        /*if (UserDao().findById(row[VehicleTable.id]) == null) {
            throw Exception("User id is null for vehicle ${row[VehicleTable.id]}.")
        }*/

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
            photoPath = row[VehicleTable.photoPath],
            totalYearlyUsageKilometers = row[VehicleTable.totalYearlyUsageKilometers],
            ownerId = row[VehicleTable.ownerId],
            tco = row[VehicleTable.tco],
        )
    }

    // Prepare a statement to create or update an entity in the database
    override fun createEntity(entity: Vehicle, statement: UpdateBuilder<Int>) {
        /*if (UserDao().findById(entity.id) == null) {
            throw Exception("User with id ${entity.ownerId} does not yet exist, so cannot create a new car with id ${entity.id}.")
        }*/

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
        statement[VehicleTable.tco] = entity.tco
    }

}