package com.example

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.*
import java.io.File

class MultiUploadFileTest {
    @Test
    fun testUploadFourImages() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.realm" to "Access protected routes",
                "ktor.jwt.secret" to "secret",
                "ktor.jwt.issuer" to "http://127.0.0.1:8080",
                "ktor.jwt.audience" to "http://127.0.0.1:8080",
                "ktor.jwt.tokenExpiry" to "86400000",
            )
        }
        application {
            module()
        }

        val boundary = "WebAppBoundary"
        val imageBytes = File("src/test/http-requests/ktor_logo.png").readBytes()

        val response = client.post("/upload/cars/1") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("description", "Four images test")
                        repeat(4) { idx ->
                            val name = "photo${idx + 1}.png"
                            append(
                                "image",
                                imageBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, "image/png")
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
        assertTrue(body.contains("Uploaded 4 file(s)"), "Expected multi-upload summary, got: $body")

        // Verify files were saved as picture_1..picture_4 under the car-specific directory
        (1..4).forEach { idx ->
            val expected = File("uploads/cars/1/picture_${idx}.png")
            assertTrue(expected.exists(), "Expected file to exist: ${expected.path}")
        }

        // Cleanup
        (1..4).forEach { idx ->
            File("uploads/cars/1/picture_${idx}.png").delete()
        }
    }

    @Test
    fun testUploadSingleImage() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.realm" to "Access protected routes",
                "ktor.jwt.secret" to "secret",
                "ktor.jwt.issuer" to "http://127.0.0.1:8080",
                "ktor.jwt.audience" to "http://127.0.0.1:8080",
                "ktor.jwt.tokenExpiry" to "86400000",
            )
        }
        application {
            module()
        }

        val boundary = "WebAppBoundary"
        val imageBytes = File("src/test/http-requests/ktor_logo.png").readBytes()

        val response = client.post("/upload/cars/1") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("description", "Ktor logo")
                        append(
                            "image",
                            imageBytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.png\"")
                            }
                        )
                    },
                    boundary,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary)
                )
            )
        }

        assertEquals("Ktor logo is uploaded to 'uploads/cars/1/picture_1.png'", response.bodyAsText(Charsets.UTF_8))

        // File should be saved with sequential name in per-car directory
        val saved = File("uploads/cars/1/picture_1.png")
        assertTrue(saved.exists(), "Expected uploaded file to be saved: ${saved.path}")
        saved.delete()
    }

    @Test
    fun testUploadNoImages() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.realm" to "Access protected routes",
                "ktor.jwt.secret" to "secret",
                "ktor.jwt.issuer" to "http://127.0.0.1:8080",
                "ktor.jwt.audience" to "http://127.0.0.1:8080",
                "ktor.jwt.tokenExpiry" to "86400000",
            )
        }
        application {
            module()
        }

        val boundary = "WebAppBoundary"

        val response = client.post("/upload/cars/1") {
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
        environment {
            config = MapApplicationConfig(
                "ktor.jwt.realm" to "Access protected routes",
                "ktor.jwt.secret" to "secret",
                "ktor.jwt.issuer" to "http://127.0.0.1:8080",
                "ktor.jwt.audience" to "http://127.0.0.1:8080",
                "ktor.jwt.tokenExpiry" to "86400000",
            )
        }
        application { module() }

        val carId = "1"
        val carDir = File("uploads/cars/$carId").apply { mkdirs() }
        val oldFile = File(carDir, "old_to_be_deleted.txt").apply { writeText("old") }
        assertTrue(oldFile.exists(), "Precondition failed: old file should exist before upload")

        val boundary = "WebAppBoundary"
        val imageBytes = File("src/test/http-requests/ktor_logo.png").readBytes()

        val response = client.post("/upload/cars/$carId") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("description", "Cleanup test")
                        append(
                            "image",
                            imageBytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.png\"")
                            }
                        )
                    },
                    boundary,
                    ContentType.MultiPart.FormData.withParameter("boundary", boundary)
                )
            )
        }

        assertEquals("Cleanup test is uploaded to 'uploads/cars/1/picture_1.png'", response.bodyAsText(Charsets.UTF_8))

        // Old file must be removed by cleanup
        assertFalse(oldFile.exists(), "Expected cleanup to remove old file: ${oldFile.path}")
        // New file must exist
        val saved = File(carDir, "picture_1.png")
        assertTrue(saved.exists(), "Expected uploaded file to be saved: ${saved.path}")

        // Cleanup
        saved.delete()
        carDir.deleteRecursively()
    }
}
