package com

import com.example.*
import com.example.models.*
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import kotlin.test.*

class VehicleRepositoryTest {
    val vehicle = Vehicle(
        id = 1L,
        make = "Toyota",
        model = "Corolla",
        year = 2020,
        category = "Sedan",
        seats = 5,
        range = 600.0,
        beginOdometer = 0.0,
        endOdometer = 0.0,
        licensePlate = "ABC-123",
        status = VehicleStatus.AVAILABLE,
        location = "Garage",
        price = 0.5,
        photoPath = "[]",
        beginReservation = null,
        endReservation = null,
        totalYearlyUsageKilometers = 0.0
    )

    @BeforeTest
    fun setup() = testApplication {
        application {
            val jwtConfig = jwtConfig()
            configureSerialization()
            configureJWTAuthentication(jwtConfig)
            configureRouting(jwtConfig)
            configureStatusPages()
            configureDatabase()
        }
        val response = client.get("/vehicles")
        assertEquals(HttpStatusCode.OK, response.status)
        if (VehicleDao().findById(1) == null) {
            VehicleDao().create(vehicle)
        }
    }

    @Test
    fun testCreateVehicle() {
        val found = VehicleDao().findById(1)
        assertNotNull(found)
        assertEquals("Toyota", found.make)
        VehicleDao().delete(1)
    }

    @Test
    fun testFindById() {
        val found = VehicleDao().findById(1)
        assertNotNull(found)
        assertEquals("Toyota", found.make)
        VehicleDao().delete(1)
    }

    @Test
    fun testFindAll() {
        val allVehicles = VehicleDao().findAll()
        assertTrue(allVehicles.any { it.make == "Toyota" })
        VehicleDao().delete(1)
    }

    @Test
    fun testDeleteVehicle() {
        VehicleDao().delete(1)
        val found = VehicleDao().findById(1)
        assertNull(found)
    }
}