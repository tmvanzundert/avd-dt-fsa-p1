package com.example.models

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

interface ReservationsRepository : CrudRepository<Reservations, Long>

class ReservationsDao :
    CrudDAO<Reservations, Long, ReservationTable>(ReservationTable),
    ReservationsRepository {

    override fun getEntity(row: ResultRow): Reservations {
        return Reservations(
            id = row[ReservationTable.id],
            user_id = row[ReservationTable.user_id],
            vehicle_id = row[ReservationTable.vehicle_id],
            rate_plan_id = row[ReservationTable.rate_plan],
            staff_id = row[ReservationTable.staff_id],
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
        statement[ReservationTable.rate_plan] = entity.rate_plan_id
        statement[ReservationTable.staff_id] = entity.staff_id
        statement[ReservationTable.start_at] = entity.start_at
        statement[ReservationTable.end_at] = entity.end_at
        statement[ReservationTable.status] = entity.status
        statement[ReservationTable.total_amount] = entity.total_amount
        statement[ReservationTable.pickup_location_id] = entity.pickup_location_id
        statement[ReservationTable.dropoff_location_id] = entity.dropoff_location_id
    }
}
