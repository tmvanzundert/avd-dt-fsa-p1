package com.example

import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.*
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.copyAndClose
import java.io.File
import java.io.IOException
import io.ktor.util.cio.writeChannel

fun Application.configureRouting() {
    routing {
        // HTTP GET route for the root path ("/"), responds with "Hello World!" for now.
        // TODO: Replace with actual content later, usage and route endpoints.
        get("/") {
            call.respondText("Hello World!")
        }

        post("/upload/cars/{carId}") {
            // todo() check for integer carId parameter
            val carId = call.parameters["carId"] ?: return@post call.respondText(
                "Missing carId",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )

            var fileDescription = ""
            val originalFileNames = mutableListOf<String>()
            val savedFileNames = mutableListOf<String>()
            val uploadDir = File("uploads/cars/$carId/").apply { mkdirs() }

            // Clean the car-specific directory before saving the new pictures
            try {
                uploadDir.listFiles()?.forEach { existing ->
                    if (!existing.deleteRecursively()) {
                        call.application.environment.log.debug("Could not delete '{}'", existing.absolutePath)
                    }
                }
            } catch (e: IOException) {
                call.application.environment.log.warn("Failed to clean directory '{}'", uploadDir.absolutePath, e)
            } catch (t: Throwable) {
                call.application.environment.log.error(
                    "Unexpected error during cleanup of '{}'",
                    uploadDir.absolutePath,
                    t
                )
            }

            val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 100)

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }

                    is PartData.FileItem -> {
                        if (savedFileNames.size < 4) {
                            val originalName = part.originalFileName ?: "upload-${System.currentTimeMillis()}"
                            // Determine extension (if any) to preserve it
                            val ext = originalName.substringAfterLast('.', "")
                            val extWithDot = if (ext.isNotEmpty()) ".${ext}" else ""
                            val index = savedFileNames.size + 1
                            val savedName = "picture_${index}${extWithDot}"
                            val file = File(uploadDir, savedName)
                            part.provider().copyAndClose(file.writeChannel())
                            originalFileNames.add(originalName)
                            savedFileNames.add(savedName)
                        }
                    }

                    else -> {}
                }

                part.dispose()
            }

            if (savedFileNames.isEmpty()) {
                return@post call.respondText(
                    "No image files were uploaded",
                    status = io.ktor.http.HttpStatusCode.BadRequest
                )
            }

            // Single-file response
            if (savedFileNames.size == 1) {
                // Return the actual saved filename with the picture_ index and per-car directory
                return@post call.respondText("${fileDescription} is uploaded to 'uploads/cars/${carId}/${savedFileNames.first()}'")
            }

            // Multi-file response (show saved names)
            val joinedSaved = savedFileNames.joinToString(", ")
            call.respondText("Uploaded ${savedFileNames.size} file(s): ${joinedSaved} to 'uploads/cars/${carId}/'")
        }
    }
}
