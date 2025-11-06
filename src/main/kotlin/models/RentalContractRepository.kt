package com.example.models

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

interface RentalContractRepository: CrudRepository<RentalContract, Long> {

}

class RentalContractDao: CrudDAO<RentalContract, Long, RentalContractTable>(RentalContractTable), RentalContractRepository {
    override fun getEntity(row: ResultRow): RentalContract {
        return RentalContract(
            id = row[RentalContractTable.id],
            vehicleId = row[RentalContractTable.vehicleId],
            pickupOdometer = row[RentalContractTable.pickupOdometer],
            dropoffOdometer = row[RentalContractTable.dropoffOdometer],
            pickupTime = row[RentalContractTable.pickupTime],
            returnTime = row[RentalContractTable.returnTime],
            signedAt = row[RentalContractTable.signedAt]
        )
    }

    override fun createEntity(
        entity: RentalContract,
        statement: UpdateBuilder<Int>
    ) {
        statement[RentalContractTable.id] = entity.id
        statement[RentalContractTable.vehicleId] = entity.vehicleId
        statement[RentalContractTable.pickupOdometer] = entity.pickupOdometer
        statement[RentalContractTable.dropoffOdometer] = entity.dropoffOdometer
        statement[RentalContractTable.pickupTime] = entity.pickupTime
        statement[RentalContractTable.returnTime] = entity.returnTime
        statement[RentalContractTable.signedAt] = entity.signedAt
    }
}