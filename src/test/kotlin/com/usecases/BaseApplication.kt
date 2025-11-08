package com.usecases

import com.example.*
import com.example.models.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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

open class BaseApplication {

    protected val vehicleDao = VehicleDao()
    protected var vehicle = Vehicle(
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

    protected val userDao = UserDao()
    protected var user = User(
        firstName = "John",
        lastName = "Doe",
        username = "jdoe",
        address = "Ginnekenweg 12, Breda",
        password = "test123",
        email = "jdoe@hotmail.com",
    )

    protected val jwtConfig = JWTConfig(
        secret = "secret",
        issuer = "http://127.0.0.1:8085",
        audience = "http://127.0.0.1:8085",
        realm = "Access protected routes",
        tokenExpiry = 86400000
    )

    protected var authToken: String = ""

    // Install all app modules once per test run
    protected fun Application.installApp() {
        configureSerialization()
        configureJWTAuthentication(jwtConfig)
        configureRouting(jwtConfig)
        configureStatusPages()
        configureDatabase()
    }

    // Start configured app and run provided block with a client
    protected fun withConfiguredApp(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        application { installApp() }
        block()
    }

    private fun createTestJwt(forUser: User): String {
        val algorithm = Algorithm.HMAC256(jwtConfig.secret)
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(forUser.id.toString())
            .withClaim("username", forUser.username)
            .sign(algorithm)
    }

    protected suspend fun ApplicationTestBuilder.seedVehicle() {
        //User
        val response = client.post("/signup") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody("""{"username":"${user.username}","password":"${user.password}", "firstName":"${user.firstName}", "lastName":"${user.lastName}", "address":"${user.address}", "email":"${user.email}"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status, "Signup should succeed; body: ${response.bodyAsText()}")

        val createdUser: User = userDao.findByUsername(user.username)
            ?: error("User '${user.username}' should exist after signup")

        // create a test JWT signed with the app secret so requests are authorized
        authToken = createTestJwt(createdUser)

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
}