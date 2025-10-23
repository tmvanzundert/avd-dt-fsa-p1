package com.example.models

import kotlin.time.ExperimentalTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.core.statements.InsertStatement


private interface UserRepository<User, Long>: CrudRepository<User, Long> {

}

@OptIn(ExperimentalTime::class)
class UserDao: UserRepository<User, Long> {

    // Delete?
    /*private var dbConnection: Database = null

    init {
        dbConnection = dbConnect()
    }*/

    override fun findAll(): List<User> {
        // Delete?
        /*val users = dbConnection.query("SELECT * FROM users")
        return users.map { row ->
            User(
                id = row["id"] as Long,
                name = row["name"] as String,
                role = Role.valueOf(row["role"] as String),
                phone = row["phone"] as String,
                password = row["password"] as String,
                email = row["email"] as String,
                rating = row["rating"] as? Float,
                createdAt = row["createdAt"] as? Instant,
                birthDate = row["birthDate"] as? Instant,
                driverLicenseNumber = row["driverLicenseNumber"] as String
            )
        }*/

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
        var user: User? = null
        transaction{
            user = UserTable.select ( UserTable.id eq id ).map {
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
            }.firstOrNull()
        }

        return user
    }

    override fun create(entity: User) {
        var newUser: InsertStatement<Number>? = null
        transaction {
            newUser = UserTable.insert {
                it[id] = entity.id
                it[name] = entity.name
                it[role] = entity.role
                it[phone] = entity.phone
                it[password] = entity.password
                it[email] = entity.email
                it[rating] = entity.rating
                it[createdAt] = entity.createdAt
                it[birthDate] = entity.birthDate
                it[driverLicenseNumber] = entity.driverLicenseNumber
            }
        }

        if (newUser == null) {
            throw Exception("Failed to create user")
        }
    }

    override fun update(entity: User) {
        val userId = findById(entity.id)?.id ?: throw Exception("User not found")

        transaction {
            UserTable.update({ UserTable.id eq userId }) {
                it[name] = entity.name
                it[role] = entity.role
                it[phone] = entity.phone
                it[password] = entity.password
                it[email] = entity.email
                it[rating] = entity.rating
                it[createdAt] = entity.createdAt
                it[birthDate] = entity.birthDate
                it[driverLicenseNumber] = entity.driverLicenseNumber
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