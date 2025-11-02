package com.example.routes

import com.example.models.*
import io.ktor.http.content.*
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.json.*
import java.io.*
import kotlin.text.toLong

fun Route.imageRoutes() {
    val vehicleDao = VehicleDao()

    post("/upload/cars/{id}") {
        // Extract carId from the URL parameters and throw error if missing
        val carId = call.parameters["id"] ?: return@post call.respondText(
            "Missing carId",
            status = io.ktor.http.HttpStatusCode.BadRequest
        )

        val id = carId.toLong()

        // Check if car exists
        if (vehicleDao.findById(id) == null) {
            return@post call.respondText(
                "Car with id='$carId' not found",
                status = io.ktor.http.HttpStatusCode.NotFound
            )
        }

        // Set the path for the upload folder
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

        var fileDescription = ""
        val originalFileNames = mutableListOf<String>()
        val savedFileNames = mutableListOf<String>()
        val savedFilePaths = mutableListOf<String>()

        // When an HTTP POST is made to this route, figure out if it's file or form data
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 100)
        multipartData.forEachPart { part ->
            when (part) {
                // Save the form data as description
                is PartData.FormItem -> {
                    fileDescription = part.value
                }

                // Save the file parts to disk with sequential naming
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
                        savedFilePaths.add("$uploadDir/$savedName")
                    }
                }

                else -> {}
            }

            part.dispose()
        }

        // Check if any files were uploaded
        if (savedFileNames.isEmpty()) {
            return@post call.respondText(
                "No image files were uploaded",
                status = io.ktor.http.HttpStatusCode.BadRequest
            )
        }

        // Serialize to JSON string and set as information that needs to be updated in the vehicle object
        val json: String = Json.encodeToJsonElement(savedFilePaths).toString()
        val vehicle: Vehicle = Vehicle(
            id = id,
            make = "",
            model = "",
            year = 0,
            category = "",
            seats = 0,
            range = 0.0,
            beginOdometer = 0.0,
            endOdometer = 0.0,
            licensePlate = "",
            status = VehicleStatus.NULL,
            location = "",
            price = 0.0,
            photoPath = json,
            beginReservation = null,
            endReservation = null,
            totalYearlyUsageKilometers = 0.0
        )

        // Update vehicle with new photo paths
        vehicleDao.update(vehicle)

        // Single-file response
        if (savedFileNames.size == 1) {
            // Return the actual saved filename with the picture_ index and per-car directory
            return@post call.respondText("$fileDescription is uploaded to 'uploads/cars/${carId}/${savedFileNames.first()}'")
        }

        // Multi-file response (show saved names)
        val joinedSaved = savedFileNames.joinToString(", ")
        call.respondText("Uploaded ${savedFileNames.size} file(s): $joinedSaved to 'uploads/cars/${carId}/'")

    }
}