package com.usecases

import com.example.models.Vehicle
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json


@OptIn(ExperimentalTime::class)
class TimebasedSearchTest: BaseApplication() {
    val startDate: LocalDateTime = LocalDateTime.parse("2021-01-02T03:15:30")
    val endDate: LocalDateTime = LocalDateTime.parse("2025-01-02T03:15:30")

    @Test
    fun `finds all vehicles within a certain date-timeframe`() {
        val filterVehicles = vehicleDao.findByTimeAvailable(startDate, endDate)
        assertEquals(filterVehicles.size, 1, "Filtered vehicles on date and expected 1 vehicle but got ${filterVehicles.size}")
    }

    @Test
    fun `filter all vehicles in route`() = withConfiguredApp {
        val response = client.get("/vehicle/$startDate/$endDate") {
            header("Authorization", "Bearer $authToken")
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }

        val filterVehicles = Json.decodeFromString<List<Vehicle>>(response.bodyAsText())
        assertEquals(filterVehicles.size, 1, "Filtered vehicles on date and expected 1 vehicle but got ${filterVehicles.size}")
    }
}