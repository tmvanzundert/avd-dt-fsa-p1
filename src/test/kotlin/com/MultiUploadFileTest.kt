package com

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import java.io.File
import kotlin.test.*

class MultiUploadFileTest : BaseApplication() {
    // Reusable test constants
    private val boundary = "WebAppBoundary"
    private val extension = "jpg"
    private val testPicture = "src/test/http-request/ktor.jpg"
    private val imageBytes = File(testPicture).readBytes()

    // Helper: build multipart body for image uploads
    private fun buildMultipartBody(
        description: String,
        files: List<Pair<String, ByteArray>>
    ): MultiPartFormDataContent =
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

    // Helper: perform upload request using BaseApplication's authToken
    private suspend fun HttpClient.uploadCarImages(
        carId: String,
        description: String,
        fileNames: List<String>
    ) = post("/upload/cars/$carId") {
        bearerAuth(authToken)
        setBody(buildMultipartBody(description, fileNames.map { it to imageBytes }))
    }

    @Test
    fun testUploadFourImages() = withConfiguredApp {
        val response = client.uploadCarImages(
            carId = "1",
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
}
