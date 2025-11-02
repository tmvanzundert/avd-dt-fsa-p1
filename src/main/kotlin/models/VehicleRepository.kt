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
    fun calculateYearlyKilometers(vehicleId: Long): Double
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

    // Calculate how much the vehicle consumes in expenses per kilometer
    override fun consumptionExpenses(vehicleId: Long): Double {
        val vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")
        return (vehicle.endOdometer - vehicle.beginOdometer) / vehicle.price
    }

    // Calculate how many kilometers the vehicle has driven in a year
    override fun calculateYearlyKilometers(vehicleId: Long): Double {
        val vehicle: Vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")

        vehicle.totalYearlyUsageKilometers = vehicle.endOdometer - vehicle.beginOdometer
        update(vehicle)
        return vehicle.totalYearlyUsageKilometers
    }

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
            beginOdometer = row[VehicleTable.beginOdometer],
            endOdometer = row[VehicleTable.endOdometer],
            licensePlate = row[VehicleTable.licensePlate],
            status = row[VehicleTable.status],
            location = row[VehicleTable.location],
            price = row[VehicleTable.price],
            photoPath = row[VehicleTable.photoPath],
            beginReservation = row[VehicleTable.beginReservation],
            endReservation = row[VehicleTable.endReservation],
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
        statement[VehicleTable.beginOdometer] = entity.beginOdometer
        statement[VehicleTable.endOdometer] = entity.endOdometer
        statement[VehicleTable.licensePlate] = entity.licensePlate
        statement[VehicleTable.status] = entity.status
        statement[VehicleTable.location] = entity.location
        statement[VehicleTable.price] = entity.price
        statement[VehicleTable.photoPath] = entity.photoPath
        statement[VehicleTable.beginReservation] = entity.beginReservation
        statement[VehicleTable.endReservation] = entity.endReservation
        statement[VehicleTable.totalYearlyUsageKilometers] = entity.totalYearlyUsageKilometers
    }

}