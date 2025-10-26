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
import io.ktor.client.request.*
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import java.io.File
import kotlin.test.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MultiUploadFileTest {
    private val boundary = "WebAppBoundary"
    private val extension = "jpg"
    private val testPicture = "src/test/http-request/ktor.jpg"
    private lateinit var authToken: String
    private val imageBytes = File(testPicture).readBytes()
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

    @BeforeTest
    fun build() = testApplication {
        application {
            configureSerialization()
            configureJWTAuthentication(jwtConfig)
            configureRouting(jwtConfig)
            configureStatusPages()
            configureDatabase()
        }
        val jsonBody = """
            {"username": "${user.username}", "password": "test123"}
        """.trimIndent()
        val response = client.post("/signup") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jsonBody)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val tokenResponse = Json.decodeFromString<TokenResponse>(response.bodyAsText())
        authToken = tokenResponse.token
    }

    @Test
    fun testUploadFourImages() = testApplication {
        application {
            configureSerialization()
            configureJWTAuthentication(jwtConfig)
            configureRouting(jwtConfig)
            configureStatusPages()
            configureDatabase()
        }
        val response = client.post("/upload/cars/1") {
            bearerAuth(authToken)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("description", "Four images test")
                        repeat(4) {
                            val name = "photo${it + 1}.$extension"
                            append(
                                "image",
                                imageBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, "image/$extension")
                                    append(HttpHeaders.ContentDisposition, "filename=\"$name\"")
                                }
                            )
                        }
                    },
                    boundary,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary)
                )
            )
        }
        val body = response.bodyAsText(Charsets.UTF_8)
        assertTrue(body.contains("Uploaded 4 file(s)"), "Expected multi-upload summary, got: '$body'")
        (1..4).forEach {
            val expected = File("uploads/cars/1/picture_${it}.$extension")
            assertTrue(expected.exists(), "Expected file to exist: ${expected.path}")
        }
        (1..4).forEach {
            File("uploads/cars/1/picture_${it}.$extension").delete()
        }
    }

    @Test
    fun testUploadSingleImage() = testApplication {
        application {
            configureSerialization()
            configureJWTAuthentication(jwtConfig)
            configureRouting(jwtConfig)
            configureStatusPages()
            configureDatabase()
        }
        val response = client.post("/upload/cars/1") {
            bearerAuth(authToken)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("description", "Ktor logo")
                        append(
                            "image",
                            imageBytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/$extension")
                                append(HttpHeaders.ContentDisposition, "filename=\"ktor.$extension\"")
                            }
                        )
                    },
                    boundary,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary)
                )
            )
        }
        assertEquals(
            "Ktor logo is uploaded to 'uploads/cars/1/picture_1.$extension'",
            response.bodyAsText(Charsets.UTF_8)
        )
        val saved = File("uploads/cars/1/picture_1.$extension")
        assertTrue(saved.exists(), "Expected uploaded file to be saved: ${saved.path}")
        saved.delete()
    }

    @Test
    fun testUploadNoImages() = testApplication {
        application {
            configureSerialization()
            configureJWTAuthentication(jwtConfig)
            configureRouting(jwtConfig)
            configureStatusPages()
            configureDatabase()
        }
        val response = client.post("/upload/cars/1") {
            bearerAuth(authToken)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("description", "No images")
                    },
                    boundary,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary)
                )
            )
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("400: Bad Request", response.bodyAsText(Charsets.UTF_8))
    }

    @Test
    fun testCleanupCarDirectoryBeforeUpload() = testApplication {
        application {
            configureSerialization()
            configureJWTAuthentication(jwtConfig)
            configureRouting(jwtConfig)
            configureStatusPages()
            configureDatabase()
        }
        val carId = "1"
        val carDir = File("uploads/cars/$carId").apply { mkdirs() }
        val oldFile = File(carDir, "old_to_be_deleted.txt").apply { writeText("old") }
        assertTrue(oldFile.exists(), "Precondition failed: old file should exist before upload")
        val response = client.post("/upload/cars/$carId") {
            bearerAuth(authToken)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("description", "Cleanup test")
                        append(
                            "image",
                            imageBytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/$extension")
                                append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.$extension\"")
                            }
                        )
                    },
                    boundary,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary)
                )
            )
        }
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
    fun end() {
        userDao.findByUsername(user.username)?.let { userDao.deleteByUsername(user.username) }
    }
}

@Serializable
data class TokenResponse(val token: String)