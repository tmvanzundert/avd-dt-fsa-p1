package com.example.routes

import com.example.models.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

/**
 * Shared multipart upload handler used by both `/upload/users/{id}` and `/upload/vehicles/{id}`.
 *
 * Contract:
 * - Reads multipart form data and stores file parts on disk.
 * - Enforces a maximum number of uploaded files (users=1, vehicles=4).
 * - Saves files under: `uploads/{entityType}/{id}/`.
 * - Persists the stored path(s) via the provided `persistPaths` callback.
 *
 * Notes:
 * - This handler intentionally *replaces* existing images for an entity by cleaning its folder first.
 * - Extra files beyond `maxFiles` are ignored (but still consumed from the request stream).
 */
private suspend fun ApplicationCall.handleImageUpload(
    // Used both for the upload directory name and error messages.
    // In this project we pass "users" or "vehicles".
    entityType: String,
    // Route param name that contains the numeric id (we use "id" in both endpoints).
    idParamName: String,
    // How many images this entity is allowed to have.
    maxFiles: Int,
    // Allows the route to validate the entity exists without coupling this function to a specific DAO.
    entityExists: (Long) -> Boolean,
    // Allows the route to decide how/where paths are stored in the DB.
    // For users we store a single string, for vehicles we store a JSON array string.
    persistPaths: (Long, List<String>) -> Unit,
) {
    // ---- Validate path parameter ----
    val idRaw = parameters[idParamName] ?: return respondText(
        "Missing $idParamName",
        status = HttpStatusCode.BadRequest
    )

    val id = idRaw.toLongOrNull() ?: return respondText(
        "Invalid $idParamName",
        status = HttpStatusCode.BadRequest
    )

    // ---- Validate entity existence ----
    if (!entityExists(id)) {
        return respondText(
            "${entityType.replaceFirstChar { it.uppercase() }} with id='$idRaw' not found",
            status = HttpStatusCode.NotFound
        )
    }

    // Directory where we store the image(s) for this entity.
    val uploadDir = File("uploads/$entityType/$id/").apply { mkdirs() }

    // Replace existing images for this entity.
    // (Simple approach: clear folder; new upload becomes the source of truth.)
    try {
        uploadDir.listFiles()?.forEach { existing ->
            if (!existing.deleteRecursively()) {
                application.environment.log.debug("Could not delete '{}'", existing.absolutePath)
            }
        }
    } catch (e: IOException) {
        application.environment.log.warn("Failed to clean directory '{}'", uploadDir.absolutePath, e)
    } catch (t: Throwable) {
        application.environment.log.error("Unexpected error during cleanup of '{}'", uploadDir.absolutePath, t)
    }

    // Optional form field (some http test files send a text description).
    var fileDescription = ""

    // We keep both names and paths for a friendly response and for persistence.
    val savedFileNames = mutableListOf<String>()
    val savedRelativePaths = mutableListOf<String>()

    // Read multipart stream. The `formFieldLimit` is the max bytes for *form fields* (not files).
    val multipartData = receiveMultipart(formFieldLimit = 1024 * 1024 * 100)

    // Consume parts sequentially (compatible across Ktor versions).
    while (true) {
        val part = multipartData.readPart() ?: break
        try {
            when (part) {
                is PartData.FormItem -> fileDescription = part.value

                is PartData.FileItem -> {
                    // Enforce max images: users=1, vehicles=4.
                    if (savedFileNames.size < maxFiles) {
                        // We do not trust the client name for file paths; we use it only to keep extension.
                        val originalName = part.originalFileName ?: "upload-${System.currentTimeMillis()}"
                        val ext = originalName.substringAfterLast('.', "").trim()
                        val extWithDot = if (ext.isNotEmpty()) ".${ext}" else ""

                        // Stable filenames make it easy to overwrite existing images when re-uploading.
                        val index = savedFileNames.size + 1
                        val savedName = if (maxFiles == 1) {
                            // User avatar
                            "avatar$extWithDot"
                        } else {
                            // Vehicle gallery
                            "picture_${index}$extWithDot"
                        }

                        val file = File(uploadDir, savedName)

                        // Stream part contents directly to disk.
                        part.provider().copyAndClose(file.writeChannel())

                        savedFileNames.add(savedName)
                        // Store relative path so it works across environments and can be served as static content.
                        savedRelativePaths.add("uploads/$entityType/$id/$savedName")
                    }
                    // else: ignore extra files (but we still consumed the part)
                }

                else -> Unit
            }
        } finally {
            // Important: always dispose parts to avoid leaks.
            part.dispose()
        }
    }

    if (savedFileNames.isEmpty()) {
        return respondText(
            "No image files were uploaded",
            status = HttpStatusCode.BadRequest
        )
    }

    // Persist paths to DB.
    persistPaths(id, savedRelativePaths)

    // ---- Simple text response (matches existing test style) ----
    if (savedFileNames.size == 1) {
        return respondText(
            "${if (fileDescription.isBlank()) "File" else fileDescription} uploaded to '${savedRelativePaths.first()}'"
        )
    }

    val joinedSaved = savedFileNames.joinToString(", ")
    respondText("Uploaded ${savedFileNames.size} file(s): $joinedSaved to 'uploads/$entityType/$id/'")
}

fun Route.imageRoutes() {
    // Vehicle photos endpoint: supports up to 4 images.
    // Persists to `vehicles.photo_path` as a JSON array string (single VARCHAR column).
    post("/upload/vehicles/{id}") {
        val vehicleDao = VehicleDao()
        call.handleImageUpload(
            entityType = "vehicles",
            idParamName = "id",
            maxFiles = 4,
            entityExists = { vehicleId -> vehicleDao.findById(vehicleId) != null },
            persistPaths = { vehicleId, paths ->
                val json = Json.encodeToString(ListSerializer(String.serializer()), paths)
                vehicleDao.updateProperty(vehicleId, "photoPath", json)
            },
        )
    }

    // User avatar endpoint: supports exactly 1 image.
    // Persists to `users.avatar_path` as a single string.
    post("/upload/users/{id}") {
        val userDao = UserDao()
        call.handleImageUpload(
            entityType = "users",
            idParamName = "id",
            maxFiles = 1,
            entityExists = { userId -> userDao.findById(userId) != null },
            persistPaths = { userId, paths ->
                userDao.updateProperty(userId, "avatarPath", paths.first())
            },
        )
    }
}