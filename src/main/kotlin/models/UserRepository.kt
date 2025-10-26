package com.example.models

import kotlin.time.ExperimentalTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.core.*
import org.mindrot.jbcrypt.BCrypt

private interface UserRepository<User, Long>: CrudRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun checkPassword(password: Password): Boolean
    fun hashPassword(password: String): String
}

@OptIn(ExperimentalTime::class)
class UserDao: UserRepository<User, Long> {

    override fun findAll(): List<User> {
        var users: List<User> = emptyList()
        transaction {
            users = UserTable.selectAll().map {
                User(
                    id = it[UserTable.id],
                    firstName = it[UserTable.firstName],
                    lastName = it[UserTable.lastName],
                    username = it[UserTable.username],
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
            throw Exception("User ${item.firstName} ${item.lastName} already exists")
        }

        transaction {
            UserTable.insert {
                it[id] = item.id
                it[firstName] = item.firstName
                it[lastName] = item.lastName
                it[username] = item.username
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
        val userId = findById(item.id) ?: throw Exception("User not found")

        transaction {
            UserTable.update({ UserTable.id eq userId.id }) {
                it[firstName] = item.firstName.ifEmpty { userId.firstName }
                it[lastName] = item.lastName.ifEmpty { userId.lastName }
                it[username] = item.username.ifEmpty { userId.username }
                it[role] = if (item.role == Role.NULL) userId.role else item.role
                it[phone] = item.phone.ifEmpty { userId.phone }
                it[password] = item.password.ifEmpty { userId.password }
                it[email] = item.email.ifEmpty { userId.email }
                it[rating] = if (item.rating == 0.0f) userId.rating else item.rating
                it[createdAt] = item.createdAt ?: userId.createdAt
                it[birthDate] = item.birthDate ?: userId.birthDate
                it[driverLicenseNumber] = item.driverLicenseNumber.ifEmpty { userId.driverLicenseNumber }
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

    override fun findByUsername(username: String): User? {
        val users = findAll()
        return users.find { it.username == username }
    }

    override fun checkPassword(password: Password): Boolean {
        val hash = password.hash
        val plainText = password.plainText ?: return false
        return BCrypt.checkpw(plainText, hash)
    }

    override fun hashPassword(password: String): String {
        val salt = BCrypt.gensalt()
        return BCrypt.hashpw(password, salt)
    }

}