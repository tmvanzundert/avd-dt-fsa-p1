package com.example.models

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

interface RatePlanRepository: CrudRepository<RatePlan, Long> {

}

class RatePlanDao: CrudDAO<RatePlan, Long, RatePlanTable>(RatePlanTable), RatePlanRepository {
    override fun getEntity(row: ResultRow): RatePlan {
        return RatePlan(
            id = row[RatePlanTable.id],
            rentalContractId = row[RatePlanTable.rentalContractId],
            name = row[RatePlanTable.name],
            pricePerDay = row[RatePlanTable.pricePerDay],
            pricePerKm = row[RatePlanTable.pricePerKm],
            deposit = row[RatePlanTable.deposit],
            cancellationPolicy = row[RatePlanTable.cancellationPolicy]
        )
    }

    override fun createEntity(
        entity: RatePlan,
        statement: UpdateBuilder<Int>
    ) {
        statement[RatePlanTable.id] = entity.id
        statement[RatePlanTable.rentalContractId] = entity.rentalContractId
        statement[RatePlanTable.name] = entity.name
        statement[RatePlanTable.pricePerDay] = entity.pricePerDay
        statement[RatePlanTable.pricePerKm] = entity.pricePerKm
        statement[RatePlanTable.deposit] = entity.deposit
        statement[RatePlanTable.cancellationPolicy] = entity.cancellationPolicy
    }
}