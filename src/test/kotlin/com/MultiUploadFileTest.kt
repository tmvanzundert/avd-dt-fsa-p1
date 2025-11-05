package com

import com.example.JWTConfig
import com.example.configureDatabase
import com.example.configureJWTAuthentication
import com.example.configureRouting
import com.example.configureSerialization
import com.example.configureStatusPages
import com.example.models.Role
import com.example.models.User
import com.example.models.UserDao
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.*

class MultiUploadFileTest {
    // Reusable test constants
    private val boundary = "WebAppBoundary"
    private val extension = "jpg"
    private val testPicture = "src/test/http-request/ktor.jpg"
    private val imageBytes = File(testPicture).readBytes()

    // Auth/token state
    private lateinit var authToken: String

    // Test user and DAO
    private val userDao = UserDao()
    private val user = User(
        id = 1L,
        firstName = "John",
        lastName = "Cina",
        username = "jcina",
        address = "123 Main St",
        role = Role.CUSTOMER,
        phone = "123-456-7890",
        password = userDao.hashPassword("test123"),
        email = "",
        driverLicenseNumber = "D1234567"
    )

    // Shared JWT config for tests
    private val jwtConfig = JWTConfig(
        secret = "secret",
        issuer = "http://127.0.0.1:8085",
        audience = "http://127.0.0.1:8085",
        realm = "Access protected routes",
        tokenExpiry = 86400000
    )

    // Helper: install all application modules for tests
    private fun Application.installApp() {
        configureSerialization()
        configureJWTAuthentication(jwtConfig)
        configureRouting(jwtConfig)
        configureStatusPages()
        configureDatabase()
    }

    // Helper: start a configured test application and run the provided block
    private fun withConfiguredApp(testBody: suspend ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application { installApp() }
            testBody()
        }

    // Helper: sign up and return token
    private suspend fun ApplicationTestBuilder.signupAndGetToken(
        username: String,
        password: String
    ): String {
        val jsonBody = """
            {"username": "$username", "password": "$password"}
        """.trimIndent()
        val response = client.post("/signup") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jsonBody)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val tokenResponse = Json.decodeFromString<TokenResponse>(response.bodyAsText())
        return tokenResponse.token
    }

    // Helper: build multipart body for image uploads
    private fun buildMultipartBody(description: String, files: List<Pair<String, ByteArray>>): MultiPartFormDataContent =
        MultiPartFormDataContent(
            formData {
                append("description", description)
                files.forEach { (name, bytes) ->
                    append(
                        key = "image",
                        value = bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, "image/$extension")
                            append(HttpHeaders.ContentDisposition, "filename=\"$name\"")
                        }
                    )
                }
            },
            boundary,
            ContentType.MultiPart.FormData.withParameter("boundary", boundary)
        )

    // Helper: perform upload request
    private suspend fun HttpClient.uploadCarImages(
        carId: String,
        token: String,
        description: String,
        fileNames: List<String>
    ) = post("/upload/cars/$carId") {
        bearerAuth(token)
        setBody(buildMultipartBody(description, fileNames.map { it to imageBytes }))
    }

    @BeforeTest
    fun build() = withConfiguredApp {
        authToken = signupAndGetToken(user.username, "test123")
    }

    @Test
    fun testUploadFourImages() = withConfiguredApp {
        val response = client.uploadCarImages(
            carId = "1",
            token = authToken,
            description = "Four images test",
            fileNames = (1..4).map { "photo${it}.$extension" }
        )
        val body = response.bodyAsText(Charsets.UTF_8)
        assertTrue(body.contains("Uploaded 4 file(s)"), "Expected multi-upload summary, got: '$body'")
        (1..4).forEach {
            val expected = File("uploads/cars/1/picture_${it}.$extension")
            assertTrue(expected.exists(), "Expected file to exist: ${expected.path}")
        }
        (1..4).forEach { File("uploads/cars/1/picture_${it}.$extension").delete() }
    }

    @Test
    fun testUploadSingleImage() = withConfiguredApp {
        val response = client.uploadCarImages(
            carId = "1",
            token = authToken,
            description = "Ktor logo",
            fileNames = listOf("ktor.$extension")
        )
        assertEquals(
            "Ktor logo is uploaded to 'uploads/cars/1/picture_1.$extension'",
            response.bodyAsText(Charsets.UTF_8)
        )
        val saved = File("uploads/cars/1/picture_1.$extension")
        assertTrue(saved.exists(), "Expected uploaded file to be saved: ${saved.path}")
        saved.delete()
    }

    @Test
    fun testUploadNoImages() = withConfiguredApp {
        val response = client.post("/upload/cars/1") {
            bearerAuth(authToken)
            setBody(buildMultipartBody(description = "No images", files = emptyList()))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("400: Bad Request", response.bodyAsText(Charsets.UTF_8))
    }

    @Test
    fun testCleanupCarDirectoryBeforeUpload() = withConfiguredApp {
        val carId = "1"
        val carDir = File("uploads/cars/$carId").apply { mkdirs() }
        val oldFile = File(carDir, "old_to_be_deleted.txt").apply { writeText("old") }
        assertTrue(oldFile.exists(), "Precondition failed: old file should exist before upload")

        val response = client.uploadCarImages(
            carId = carId,
            token = authToken,
            description = "Cleanup test",
            fileNames = listOf("ktor_logo.$extension")
        )
        assertEquals(
            "Cleanup test is uploaded to 'uploads/cars/1/picture_1.$extension'",
            response.bodyAsText(Charsets.UTF_8)
        )
        assertFalse(oldFile.exists(), "Expected cleanup to remove old file: ${oldFile.path}")
        val saved = File(carDir, "picture_1.$extension")
        assertTrue(saved.exists(), "Expected uploaded file to be saved: ${saved.path}")
        saved.delete()
        carDir.deleteRecursively()
    }

    @AfterTest
    fun end() = withConfiguredApp {
        userDao.findByUsername(user.username)?.let { userDao.deleteByUsername(user.username) }
    }
}

@Serializable
data class TokenResponse(val token: String)