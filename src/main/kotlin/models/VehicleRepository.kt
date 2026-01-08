package com.example.models

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

interface VehicleRepository : CrudRepository<Vehicle, Long> {
    fun isAvailable(vehicleId: Long): Boolean
    fun findByTimeAvailable(beginDate: LocalDateTime, endDate: LocalDateTime): List<Vehicle>
    fun setLocation(vehicleId: Long, longitude: Double, latitude: Double)
}

class VehicleDao : CrudDAO<Vehicle, Long, VehicleTable>(VehicleTable), VehicleRepository {

    override fun isAvailable(vehicleId: Long): Boolean {
        val vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")
        return vehicle.status == VehicleStatus.AVAILABLE
    }

    override fun findByTimeAvailable(beginDate: LocalDateTime, endDate: LocalDateTime): List<Vehicle> {
        val vehicles: List<Vehicle> = findAll()
        return vehicles.filter { v ->
            val from = v.beginAvailable
            val to = v.endAvailable
            from != null && to != null && beginDate >= from && endDate <= to
        }
    }

    override fun setLocation(vehicleId: Long, longitude: Double, latitude: Double) {
        val vehicle = findById(vehicleId) ?: throw Exception("Vehicle not found")
        update(
            vehicle.copy(
                longitude = longitude,
                latitude = latitude
            )
        )
    }

    override fun getEntity(row: ResultRow): Vehicle {
        return Vehicle(
            id = row[VehicleTable.id],
            rangeKm = row[VehicleTable.rangeKm],
            licensePlate = row[VehicleTable.licensePlate],
            status = row[VehicleTable.status],
            longitude = row[VehicleTable.longitude],
            latitude = row[VehicleTable.latitude],
            ownerId = row[VehicleTable.ownerId],
            beginAvailable = row[VehicleTable.beginAvailable],
            endAvailable = row[VehicleTable.endAvailable],
            pricePerDay = row[VehicleTable.pricePerDay],
            photoPath = row[VehicleTable.photoPath],
        )
    }

    override fun createEntity(entity: Vehicle, statement: UpdateBuilder<Int>) {
        statement[VehicleTable.licensePlate] = entity.licensePlate
        statement[VehicleTable.rangeKm] = entity.rangeKm
        statement[VehicleTable.status] = entity.status
        statement[VehicleTable.longitude] = entity.longitude
        statement[VehicleTable.latitude] = entity.latitude
        statement[VehicleTable.ownerId] = entity.ownerId
        statement[VehicleTable.beginAvailable] = entity.beginAvailable
        statement[VehicleTable.endAvailable] = entity.endAvailable
        statement[VehicleTable.pricePerDay] = entity.pricePerDay
        statement[VehicleTable.photoPath] = entity.photoPath
    }

}