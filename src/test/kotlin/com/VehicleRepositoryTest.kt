package com

import com.example.*
import com.example.models.*
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.testing.*
import kotlin.test.*

class VehicleRepositoryTest {

    private val vehicleDao = VehicleDao()
    private var vehicle = Vehicle(
        make = "Toyota",
        model = "Corolla",
        year = 2020,
        category = "Sedan",
        seats = 5,
        range = 600.0,
        licensePlate = "TEST-PLATE",
        status = VehicleStatus.AVAILABLE,
        location = 1L,
        ownerId = 1L,
        photoPath = "[]",
        totalYearlyUsageKilometers = 0L,
        tco = 0.0
    )

    private val userDao = UserDao()
    private var user = User(
        firstName = "John",
        lastName = "Cina",
        username = "jcina",
        address = "123 Main St",
        password = "test123",
        email = "jcina@hotmail.com",
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

    private suspend fun ApplicationTestBuilder.seedVehicle() {
        //User
        val response = client.post("/signup") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody("""{"username":"${user.username}","password":"${user.password}", "firstName":"${user.firstName}", "lastName":"${user.lastName}", "address":"${user.address}", "email":"${user.email}"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status, "Signup should succeed; body: ${response.bodyAsText()}")

        val createdUser: User = userDao.findByUsername(user.username)
            ?: error ("User '${user.username}' should exist after signup")

        /*val seededUser = user.copy(id = createdUser.id, password = createdUser.password)
        user = seededUser*/

        // Vehicle
        client.get("/vehicle")

        val uniquePlate = "TST-${System.currentTimeMillis()}"
        val seededVehicle = vehicle.copy(licensePlate = uniquePlate, ownerId = createdUser.id)
        vehicleDao.create(seededVehicle)

        val createdVehicle = vehicleDao.findAll().first { it.licensePlate == uniquePlate }
        vehicle = seededVehicle.copy(id = createdVehicle.id)
    }

    @BeforeTest
    fun setup() = withConfiguredApp { seedVehicle() }

    @AfterTest
    fun cleanup() = withConfiguredApp {
        runCatching { userDao.deleteByUsername(user.username) }
        runCatching { if (user.id != 0L) userDao.delete(user.id) }
        runCatching { if (vehicle.id != 0L) vehicleDao.delete(vehicle.id) }
    }

    @Test
    fun testCreateVehicle() = withConfiguredApp {
        val found = vehicleDao.findById(vehicle.id)
        assertNotNull(found)
        assertEquals("Toyota", found.make)
    }

    @Test
    fun testFindById() = withConfiguredApp {
        val found = vehicleDao.findById(vehicle.id)
        assertNotNull(found)
        assertEquals("Toyota", found.make)
    }

    @Test
    fun testFindAll() = withConfiguredApp {
        val allVehicles = vehicleDao.findAll()
        assertTrue(allVehicles.any { it.id == vehicle.id && it.make == "Toyota" })
    }

    @Test
    fun testDeleteVehicle() = withConfiguredApp {
        vehicleDao.delete(vehicle.id)
        val found = vehicleDao.findById(vehicle.id)
        assertNull(found)
    }
}
