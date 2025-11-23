package com.example.models

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import com.example.models.NotificationDao
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime


interface ReservationsRepository : CrudRepository<Reservations, Long> {

    fun reserveCar(carId: Long, userId: Long, startTime: LocalDateTime, endTime: LocalDateTime)
    fun isCarReserved(vehicleId: Long): Boolean
    fun getCarReservationStatus(vehicleId: Long): String
    fun makeCarReservable(vehicleId: Long)
}

class ReservationsDao :
    CrudDAO<Reservations, Long, ReservationTable>(ReservationTable),
    ReservationsRepository {

    override fun getEntity(row: ResultRow): Reservations {
        return Reservations(
            id = row[ReservationTable.id],
            user_id = row[ReservationTable.user_id],
            vehicle_id = row[ReservationTable.vehicle_id],
            rate_plan_id = row[ReservationTable.rate_plan_id],
            renter_id = row[ReservationTable.renter_id],
            start_at = row[ReservationTable.start_at],
            end_at = row[ReservationTable.end_at],
            status = row[ReservationTable.status],
            total_amount = row[ReservationTable.total_amount],
            pickup_location_id = row[ReservationTable.pickup_location_id],
            dropoff_location_id = row[ReservationTable.dropoff_location_id],
        )
    }

    override fun createEntity(entity: Reservations, statement: UpdateBuilder<Int>) {
        statement[ReservationTable.id] = entity.id
        statement[ReservationTable.user_id] = entity.user_id
        statement[ReservationTable.vehicle_id] = entity.vehicle_id
        statement[ReservationTable.rate_plan_id] = entity.rate_plan_id
        statement[ReservationTable.renter_id] = entity.renter_id
        statement[ReservationTable.start_at] = entity.start_at
        statement[ReservationTable.end_at] = entity.end_at
        statement[ReservationTable.status] = entity.status
        statement[ReservationTable.total_amount] = entity.total_amount
        statement[ReservationTable.pickup_location_id] = entity.pickup_location_id
        statement[ReservationTable.dropoff_location_id] = entity.dropoff_location_id
    }

    @OptIn(ExperimentalTime::class)
    override fun reserveCar(carId: Long, userId: Long, startTime: LocalDateTime, endTime: LocalDateTime) {
        val vehicleDao = VehicleDao()
        val vehicleOwnerId = vehicleDao.findById(carId)?.ownerId ?: throw Exception("Vehicle not found")

        updateProperty(carId, "start_at", startTime)
        updateProperty(carId, "end_at", endTime)

        val user = UserDao().findById(userId) ?: throw Exception("User not found")
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC)
        val notificationDao = NotificationDao()
//        Send to renter
        notificationDao.createNotification(
            userId = user.id,
            type = "INFO",
            message = "Je reservering is bevestigd. (van ${startTime} tot ${endTime})",
            timestamp = now
        )
//        Send to rentee
        notificationDao.createNotification(
            userId = vehicleOwnerId,
            type = "INFO",
            message = "De reservering van uw auto is bevestigd. (van ${startTime} tot ${endTime})",
            timestamp = now
        )

        val reservation = findAll().firstOrNull { it.vehicle_id == carId }
            ?: throw Exception("Reservation not found for vehicle $carId")
        vehicleDao.updateProperty(reservation.vehicle_id, "status", VehicleStatus.RENTED)
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
}
