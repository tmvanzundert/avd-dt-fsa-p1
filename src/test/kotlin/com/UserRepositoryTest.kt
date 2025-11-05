package com

import com.example.JWTConfig
import com.example.configureDatabase
import com.example.configureJWTAuthentication
import com.example.configureRouting
import com.example.configureSerialization
import com.example.configureStatusPages
import com.example.models.Password
import com.example.models.Role
import com.example.models.User
import com.example.models.UserDao
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.*

class UserRepositoryTest {

    private val userDao = UserDao()
    private val user = User(
        id = 1L,
        firstName = "John",
        lastName = "Cina",
        username = "jcina",
        address = "123 Main St",
        role = Role.USER,
        phone = "123-456-7890",
        password = userDao.hashPassword("test123"),
        email = "",
        driverLicenseNumber = "D1234567"
    )

    private val jwtConfig = JWTConfig(
        secret = "secret",
        issuer = "http://127.0.0.1:8085",
        audience = "http://127.0.0.1:8085",
        realm = "Access protected routes",
        tokenExpiry = 86400000
    )

    // Install all app modules once per test run
    private fun Application.installApp() {
        configureSerialization()
        configureJWTAuthentication(jwtConfig)
        configureRouting(jwtConfig)
        configureStatusPages()
        configureDatabase()
    }

    // Start configured app and run provided block with a client
    private fun withConfiguredApp(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application { installApp() }
        block()
    }

    // Sign up the user and then seed full profile fields via update
    private suspend fun ApplicationTestBuilder.signupAndSeedUser(u: User) {
        val response = client.post("/signup") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody("""{"username":"${u.username}","password":"test123"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status, "Signup should succeed; body: ${response.bodyAsText()}")
        // Enrich the just-created user with full details
        // Note: update uses DAO transaction internally
        try {
            userDao.update(u)
        } catch (_: Throwable) {
            // If update fails because record not found (e.g., different id), try create
            runCatching { userDao.create(u) }
        }
    }

    @BeforeTest
    fun setup() = withConfiguredApp { signupAndSeedUser(user) }

    @AfterTest
    fun cleanup() = withConfiguredApp {
        runCatching { userDao.deleteByUsername(user.username) }
        runCatching { userDao.delete(1) }
    }

    @Test
    fun testCreateUser() = withConfiguredApp {
        val found = userDao.findById(1)
        assertNotNull(found)
        assertEquals("John Cina", "${found.firstName} ${found.lastName}")
    }

    @Test
    fun testFindById() = withConfiguredApp {
        val found = userDao.findById(1)
        assertNotNull(found)
        assertEquals("John Cina", "${found.firstName} ${found.lastName}")
    }

    @Test
    fun testFindAll() = withConfiguredApp {
        val allUsers = userDao.findAll()
        assertTrue(allUsers.any { "${it.firstName} ${it.lastName}" == "John Cina" })
    }

    @Test
    fun testPasswordUser() = withConfiguredApp {
        val password = Password(hash = user.password, plainText = "test123")
        assertTrue(userDao.checkPassword(password))
    }
}