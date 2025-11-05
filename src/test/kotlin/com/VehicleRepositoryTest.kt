package com

import com.example.*
import com.example.models.*
import io.ktor.client.request.get
import io.ktor.server.application.Application
import io.ktor.server.testing.*
import kotlin.test.*

class VehicleRepositoryTest {

    private val vehicleDao = VehicleDao()

    // Base vehicle with correct fields according to models/Vehicle.kt
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

    // Seed a vehicle and capture its DB id; warm up engine to ensure DB init runs
    private suspend fun ApplicationTestBuilder.seedVehicle() {
        // Warm engine so DB hooks run (mirrors UserRepositoryTest behavior)
        client.get("/vehicles")

        val uniquePlate = "TST-${System.currentTimeMillis()}"
        val seeded = vehicle.copy(licensePlate = uniquePlate)
        vehicleDao.create(seeded)

        val created = vehicleDao.findAll().first { it.licensePlate == uniquePlate }
        vehicle = seeded.copy(id = created.id)
    }

    @BeforeTest
    fun setup() = withConfiguredApp { seedVehicle() }

    @AfterTest
    fun cleanup() = withConfiguredApp {
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
