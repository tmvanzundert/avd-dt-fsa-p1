package com.example.models

import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

private interface PaymentRepository: CrudRepository<Payment, Int> {

}

class PaymentDao: PaymentRepository {
    override fun findAll(): List<Payment> {
        var payments: List<Payment> = emptyList()
        transaction {
            payments = PaymentTable.selectAll().map {
                Payment(
                    id = it[PaymentTable.id],
                    amount = it[PaymentTable.amount],
                    currency = it[PaymentTable.currency],
                    provider = it[PaymentTable.provider],
                    status = it[PaymentTable.status]
                )
            }
        }

        return payments
    }

    override fun findById(id: Int): Payment? {
        val payments = findAll()
        return payments.find { it.id.toString() == id.toString() }
    }

    override fun create(item: Payment) {
        // Check if user already exists
        findById(item.id)?.id ?.let {
            throw Exception("User ${item.name} already exists")
        }
    }

    override fun update(item: Payment) {
        TODO("Not yet implemented")
    }

    override fun delete(id: Int) {
        TODO("Not yet implemented")
    }
}