package com.example.models

import kotlin.time.ExperimentalTime

private interface UserRepository<User, Long>: CrudRepository<User, Long> {

}

@OptIn(ExperimentalTime::class)
abstract class UserDao: UserRepository<User, Long> {

    // Delete?
    /*private var dbConnection: Database = null

    init {
        dbConnection = dbConnect()
    }*/

    override suspend fun findAll(): List<User> {
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
            users = User.selectAll().map {
                User(
                    id = it[User.id],
                    name = it[User.name],
                    role = it[User.role],
                    phone = it[User.phone],
                    password = it[User.password],
                    email = it[User.email],
                    rating = it[User.rating],
                    createdAt = it[User.createdAt],
                    birthDate = it[User.birthDate],
                    driverLicenseNumber = it[User.driverLicenseNumber]
                )
            }
        }

        return users
    }

    override suspend fun findById(id: Long): User? {
        var user: User? = null
        transaction{
            user = User.select { User.id eq id }.map {
                User(
                    id = it[User.id],
                    name = it[User.name],
                    role = it[User.role],
                    phone = it[User.phone],
                    password = it[User.password],
                    email = it[User.email],
                    rating = it[User.rating],
                    createdAt = it[User.createdAt],
                    birthDate = it[User.birthDate],
                    driverLicenseNumber = it[User.driverLicenseNumber]
                )
            }.firstOrNull()
        }

        return user
    }

    override suspend fun create(entity: User) {
        var newUser: User? = null
        transaction {
            newUser = User.insert {
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

    override suspend fun update(entity: User) {
        val userId = findById(entity.id) ?: throw Exception("User not found")

        transaction {
            User.update({ User.id eq userId.id }) {
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

    override suspend fun delete(id: Long) {
        val userId = findById(id) ?: throw Exception("User not found")

        transaction {
            val deleteUser = User.deleteWhere { User.id eq userId.id }
        }

        if (deleteUser == 0) {
            throw Exception("Failed to delete user")
        }
    }

}