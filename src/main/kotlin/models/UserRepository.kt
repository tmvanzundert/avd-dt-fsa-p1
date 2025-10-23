package com.example.models

import kotlin.time.ExperimentalTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.*

private interface UserRepository<User, Long>: CrudRepository<User, Long> {

}

@OptIn(ExperimentalTime::class)
class UserDao: UserRepository<User, Long> {

    override fun findAll(): List<User> {
        var users: List<User> = emptyList()
        transaction {
            users = UserTable.selectAll().map {
                User(
                    id = it[UserTable.id],
                    name = it[UserTable.name],
                    role = it[UserTable.role],
                    phone = it[UserTable.phone],
                    password = it[UserTable.password],
                    email = it[UserTable.email],
                    rating = it[UserTable.rating],
                    createdAt = it[UserTable.createdAt],
                    birthDate = it[UserTable.birthDate],
                    driverLicenseNumber = it[UserTable.driverLicenseNumber]
                )
            }
        }

        return users
    }

    override fun findById(id: Long): User? {
        val users = findAll()
        return users.find { it.id == id }
    }

    override fun create(item: User) {
        // Check if user already exists
        findById(item.id)?.id ?.let {
            throw Exception("User ${item.name} already exists")
        }

        transaction {
            UserTable.insert {
                it[id] = item.id
                it[name] = item.name
                it[role] = item.role
                it[phone] = item.phone
                it[password] = item.password
                it[email] = item.email
                it[rating] = item.rating
                it[createdAt] = item.createdAt
                it[birthDate] = item.birthDate
                it[driverLicenseNumber] = item.driverLicenseNumber
            }
        }
    }

    override fun update(item: User) {
        val userId = findById(item.id)?.id ?: throw Exception("User not found")

        transaction {
            UserTable.update({ UserTable.id eq userId }) {
                it[name] = item.name
                it[role] = item.role
                it[phone] = item.phone
                it[password] = item.password
                it[email] = item.email
                it[rating] = item.rating
                it[createdAt] = item.createdAt
                it[birthDate] = item.birthDate
                it[driverLicenseNumber] = item.driverLicenseNumber
            }
        }
    }

    override fun delete(id: Long) {
        val userId = findById(id)?.id ?: throw Exception("User not found")

        var deleteUser = 0
        transaction {
            deleteUser = UserTable.deleteWhere { UserTable.id eq userId }
        }

        if (deleteUser == 0) {
            throw Exception("Failed to delete user")
        }
    }

}