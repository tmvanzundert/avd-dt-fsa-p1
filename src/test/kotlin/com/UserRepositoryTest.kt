package com

import com.example.*
import com.example.models.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.testing.*
import kotlin.test.*

class UserRepositoryTest {

    private val userDao = UserDao()
    private var user = User(
        firstName = "John",
        lastName = "Cina",
        username = "jcina",
        address = "123 Main St",
        role = Role.CUSTOMER,
        phone = "123-456-7890",
        password = userDao.hashPassword("test123"),
        driverLicenseNumber = "D1234567",
        email = "jcina@hotmail.com",
        rating = 0.0f,
        birthDate = null
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

    private fun resolveByUsername(username: String): User? =
        userDao.findAll().firstOrNull { it.username == username }

    // Sign up the user and then seed full profile fields via update
    private suspend fun ApplicationTestBuilder.signupAndSeedUser(u: User) {
        val response = client.post("/signup") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody("""{"username":"${user.username}","password":"test123"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status, "Signup should succeed; body: ${response.bodyAsText()}")

        val created = resolveByUsername(user.username)
            ?: error ("User '${user.username}' should exist after signup")

        val seeded = user.copy(id = created.id, password = created.password)
        userDao.update(seeded)

        user = seeded
    }

    @BeforeTest
    fun setup() = withConfiguredApp { signupAndSeedUser(user) }

    @AfterTest
    fun cleanup() = withConfiguredApp {
        runCatching { userDao.deleteByUsername(user.username) }
        runCatching { if (user.id != 0L) userDao.delete(user.id) }
    }

    @Test
    fun testLoginUser() = withConfiguredApp { signupAndSeedUser(user) }

    @Test
    fun testCreateUser() = withConfiguredApp {
        val found = userDao.findById(user.id)
        assertNotNull(found)
        assertEquals("John Cina", "${found.firstName} ${found.lastName}")
    }

    @Test
    fun testFindById() = withConfiguredApp {
        val found = userDao.findById(user.id)
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