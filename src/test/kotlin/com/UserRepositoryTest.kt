package com

import com.example.*
import com.example.models.*
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import kotlin.test.*

class UserRepositoryTest {

    val user = User(
        id = 1L,
        name = "John Cina",
        role = Role.USER,
        phone = "123-456-7890",
        password = "password123",
        email = "",
        driverLicenseNumber = "D1234567"
    )

    @BeforeTest
    fun testGetUsersEndpoint() = testApplication {
        application {
            configureSerialization()
            configureRouting()
            configureStatusPages()
            configureDatabase()
        }
        val response = client.get("/users")
        assertEquals(HttpStatusCode.OK, response.status)
        if (UserDao().findById(1) == null) {
            UserDao().create(user)
        }
    }

    @Test
    fun testCreateUser() {
        val found = UserDao().findById(1)
        assertNotNull(found)
        assertEquals("John Cina", found.name)
        UserDao().delete(1)
    }

    @Test
    fun testFindById() {
        val found = UserDao().findById(1)
        assertNotNull(found)
        assertEquals("John Cina", found.name)
        UserDao().delete(1)
    }

    @Test
    fun testFindAll() {
        val allUsers = UserDao().findAll()
        assertTrue(allUsers.any { it.name == "John Cina" })
        UserDao().delete(1)
    }

    @Test
    fun testDeleteUser() {
        UserDao().delete(1)
        val found = UserDao().findById(1)
        assertNull(found)
    }
}