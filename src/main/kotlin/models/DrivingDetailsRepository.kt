package com.example.models

interface DrivingDetailsRepository : CrudRepository<DrivingDetails, Long>  {
}

class DrivingDetailsDao :
    CrudDAO<DrivingDetails, Long, DrivingTable>(DrivingTable),
    DrivingDetailsRepository {

    override fun getEntity(row: org.jetbrains.exposed.v1.core.ResultRow): DrivingDetails {
        return DrivingDetails(
            id = row[DrivingTable.id],
            renterId = row[DrivingTable.renterId],
            vehicleId = row[DrivingTable.vehicleId],
            startTime = row[DrivingTable.startTime],
            endTime = row[DrivingTable.endTime],
            startLongitude = row[DrivingTable.startLongitude],
            startLatitude = row[DrivingTable.startLatitude],
            endLongitude = row[DrivingTable.endLongitude],
            endLatitude = row[DrivingTable.endLatitude],
        )
    }

    override fun createEntity(entity: DrivingDetails, statement: org.jetbrains.exposed.v1.core.statements.UpdateBuilder<Int>) {
        statement[DrivingTable.renterId] = entity.renterId
        statement[DrivingTable.vehicleId] = entity.vehicleId
        statement[DrivingTable.startTime] = entity.startTime
        statement[DrivingTable.endTime] = entity.endTime
        statement[DrivingTable.startLongitude] = entity.startLongitude
        statement[DrivingTable.startLatitude] = entity.startLatitude
        statement[DrivingTable.endLongitude] = entity.endLongitude
        statement[DrivingTable.endLatitude] = entity.endLatitude
    }
}