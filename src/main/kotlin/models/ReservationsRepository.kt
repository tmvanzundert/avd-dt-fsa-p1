package com.example.models

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder


interface ReservationsRepository : CrudRepository<Reservations, Long> {
    fun reserveCar(carId: Long, userId: Long, startTime: LocalDateTime, endTime: LocalDateTime)
    fun isCarReserved(vehicleId: Long): Boolean
    fun getCarReservationStatus(vehicleId: Long): String
    fun makeCarReservable(vehicleId: Long)
    fun cancelReservation(reservationId: Long)
    fun getVehicleReservations(renterId: Long, status: List<ReservationStatus>): List<Vehicle>
}

class ReservationsDao :
    CrudDAO<Reservations, Long, ReservationTable>(ReservationTable),
    ReservationsRepository {

    override fun getEntity(row: ResultRow): Reservations {
        return Reservations(
            id = row[ReservationTable.id],
            userId = row[ReservationTable.userId],
            vehicleId = row[ReservationTable.vehicleId],
            startAt = row[ReservationTable.startAt],
            endAt = row[ReservationTable.endAt],
            status = row[ReservationTable.status],
            totalAmount = row[ReservationTable.totalAmount],
            photoVehicleBefore = row[ReservationTable.photoVehicleBefore],
            photoVehicleAfter = row[ReservationTable.photoVehicleAfter],
        )
    }

    override fun createEntity(entity: Reservations, statement: UpdateBuilder<Int>) {
        statement[ReservationTable.userId] = entity.userId
        statement[ReservationTable.vehicleId] = entity.vehicleId
        statement[ReservationTable.startAt] = entity.startAt
        statement[ReservationTable.endAt] = entity.endAt
        statement[ReservationTable.status] = entity.status
        statement[ReservationTable.totalAmount] = entity.totalAmount
        statement[ReservationTable.photoVehicleBefore] = entity.photoVehicleBefore
        statement[ReservationTable.photoVehicleAfter] = entity.photoVehicleAfter
    }

    override fun reserveCar(carId: Long, userId: Long, startTime: LocalDateTime, endTime: LocalDateTime) {
        val vehicleDao = VehicleDao()
        vehicleDao.findById(carId) ?: throw Exception("Vehicle not found")

        // Simple rule: one reservation row per vehicle (update if exists, else create)
        val existing = findAll().firstOrNull { it.vehicleId == carId }

        if (existing != null) {
            update(
                existing.copy(
                    userId = userId,
                    startAt = startTime,
                    endAt = endTime,
                    status = ReservationStatus.CONFIRMED,
                )
            )
        } else {
            create(
                Reservations(
                    userId = userId,
                    vehicleId = carId,
                    startAt = startTime,
                    endAt = endTime,
                    status = ReservationStatus.CONFIRMED,
                )
            )
        }

        // Mark vehicle as rented
        vehicleDao.updateProperty(carId, "status", VehicleStatus.RENTED)
    }

    override fun isCarReserved(vehicleId: Long): Boolean {
        val vehicleDao = VehicleDao()
        val vehicle = vehicleDao.findById(vehicleId) ?: throw Exception("Vehicle not found")
        return vehicle.status == VehicleStatus.RENTED || vehicle.status == VehicleStatus.MAINTENANCE
    }

    override fun getCarReservationStatus(vehicleId: Long): String {
        val vehicleDao = VehicleDao()
        val vehicle = vehicleDao.findById(vehicleId) ?: throw Exception("Vehicle not found")
        return vehicle.status.toString()
    }

    override fun makeCarReservable(vehicleId: Long) {
        val vehicleDao = VehicleDao()
        vehicleDao.findById(vehicleId) ?: throw Exception("Vehicle not found")
        vehicleDao.updateProperty(vehicleId, "status", VehicleStatus.AVAILABLE)
    }

    override fun cancelReservation(reservationId: Long) {
        val vehicleId = findById(reservationId)?.vehicleId ?: throw Exception("Reservation not found")
        val vehicleDao = VehicleDao()

        vehicleDao.findById(vehicleId) ?: throw Exception("Vehicle not found")
        updateProperty(reservationId, "status", ReservationStatus.CANCELLED)
        vehicleDao.updateProperty(vehicleId, "status", VehicleStatus.AVAILABLE)
    }

    override fun getVehicleReservations(renterId: Long, status: List<ReservationStatus>): List<Vehicle> {
        val vehicleDao = VehicleDao()
        val reservations = findAll().filter { it.userId == renterId && status.contains(it.status) }
        return reservations.mapNotNull { reservation ->
            reservation.vehicleId?.let { vehicleDao.findById(it) }
        }
    }
}
