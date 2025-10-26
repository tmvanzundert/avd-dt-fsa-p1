package com

import com.example.*
import com.example.jwtConfig
import com.example.models.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.server.application.Application
import io.ktor.server.testing.*
import io.netty.handler.codec.http.HttpHeaders.setHeader
import kotlin.test.*
import kotlinx.serialization.Serializable
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.*

class UserRepositoryTest {

    val user = User(
        id = 1L,
        firstName = "John",
        lastName = "Cina",
        username = "jcina",
        address = "123 Main St",
        role = Role.USER,
        phone = "123-456-7890",
        password = UserDao().hashPassword("test123"),
        email = "",
        driverLicenseNumber = "D1234567"
    )

    val jwtConfig = this.jwtConfig()
    @BeforeTest
    fun testGetUsersEndpoint() = testApplication {
        application {
            configureSerialization()
            configureJWTAuthentication(jwtConfig)
            configureRouting(jwtConfig)
            configureStatusPages()
            configureDatabase()
        }

        val jsonBody = """
            {
                "username": "${user.username}",
                "password": "test123"
            }
        """.trimIndent()
        val response = client.post("/signup") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jsonBody)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        UserDao().update(user)
    }

    @Test
    fun testCreateUser() {
        val found = UserDao().findById(1)
        assertNotNull(found)
        assertEquals("John Cina", "${found.firstName} ${found.lastName}")
        UserDao().delete(1)
    }

    @Test
    fun testFindById() {
        val found = UserDao().findById(1)
        assertNotNull(found)
        assertEquals("John Cina", "${found.firstName} ${found.lastName}")
        UserDao().delete(1)
    }

    @Test
    fun testFindAll() {
        val allUsers = UserDao().findAll()
        assertTrue(allUsers.any { "${it.firstName} ${it.lastName}" == "John Cina" })
        UserDao().delete(1)
    }

    @Test
    fun testPasswordUser() {
        val password = Password(
            hash = user.password,
            plainText = "test123"
        )

        val isPasswordValid = UserDao().checkPassword(password)
        assertTrue(isPasswordValid)
        UserDao().delete(1)
    }

    @Test
    fun testDeleteUser() {
        UserDao().delete(1)
        val found = UserDao().findById(1)
        assertNull(found)
    }

    fun jwtConfig(): JWTConfig {
        return JWTConfig(
            secret = "secret",
            issuer = "http://127.0.0.1:8085",
            audience = "http://127.0.0.1:8085",
            realm = "Access protected routes",
            tokenExpiry = 86400000
        )
    }
}