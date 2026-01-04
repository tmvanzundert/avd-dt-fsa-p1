package com.example.models

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.mindrot.jbcrypt.BCrypt

// Implement these functions in the User DAO
private interface UserRepository<User, Long>: CrudRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun checkPassword(password: Password): Boolean
    fun hashPassword(password: String): String
    fun deleteByUsername(username: String)
    fun setCreatedAt(id: Long)
}

class UserDao: CrudDAO<User, Long, UserTable>(UserTable), UserRepository<User, Long> {

    // Delete a user by username
    override fun deleteByUsername(username: String) {
        val users = findAll()
        val userId = users.find { it.username == username }?.id ?: throw Exception("User not found")
        delete(userId)
    }

    // Find a user by username
    override fun setCreatedAt(id: Long) {
        val currentDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        updateProperty(id, "createdAt", currentDateTime)
    }

    override fun findByUsername(username: String): User? {
        val users = findAll()
        return users.find { it.username == username }
    }

    // Check if the password is correct
    override fun checkPassword(password: Password): Boolean {
        val hash = password.hash
        val plainText = password.plainText ?: return false
        return BCrypt.checkpw(plainText, hash)
    }

    // Hash a password for secure storage
    override fun hashPassword(password: String): String {
        val salt = BCrypt.gensalt()
        return BCrypt.hashpw(password, salt)
    }

    // Map all the database columns to the User data class
    override fun getEntity(row: ResultRow): User {
        return User(
            id = row[UserTable.id],
            firstName = row[UserTable.firstName],
            lastName = row[UserTable.lastName],
            username = row[UserTable.username],
            address = row[UserTable.address],
            role = row[UserTable.role],
            phone = row[UserTable.phone],
            password = row[UserTable.password],
            email = row[UserTable.email],
            rating = row[UserTable.rating],
            createdAt = row[UserTable.createdAt],
            birthDate = row[UserTable.birthDate],
            driverLicenseNumber = row[UserTable.driverLicenseNumber],
            avatarPath = row[UserTable.avatarPath],
        )
    }

    // Prepare a statement to create or update an entity in the database
    override fun createEntity(entity: User, statement: UpdateBuilder<Int>) {
        statement[UserTable.id] = entity.id
        statement[UserTable.firstName] = entity.firstName
        statement[UserTable.lastName] = entity.lastName
        statement[UserTable.username] = entity.username
        statement[UserTable.address] = entity.address
        statement[UserTable.role] = entity.role
        statement[UserTable.phone] = entity.phone
        statement[UserTable.password] = entity.password
        statement[UserTable.email] = entity.email
        statement[UserTable.rating] = entity.rating
        statement[UserTable.createdAt] = entity.createdAt
        statement[UserTable.birthDate] = entity.birthDate
        statement[UserTable.driverLicenseNumber] = entity.driverLicenseNumber
        statement[UserTable.avatarPath] = entity.avatarPath
    }

}