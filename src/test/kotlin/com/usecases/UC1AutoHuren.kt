package com.usecases

import com.example.models.VehicleTable.location
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.Test
import kotlin.test.assertEquals

// Test cases for UC1: Auto Huren (Car Rental)
// happy flows
// Log in as a registered user (Already implemented)
// Search for available cars based on criteria (date, location, car type)
// Select a car and proceed to booking (grab the vehicle)
// Enter personal and payment details (log a booking)
// renter receive a confirmation message (can this be done in a web-api?)
// Send a notification to the owner about the booking (can this be done in a web-api?)

class UC1AutoHuren : BaseApplication() {

    @Test
    fun `search available cars returns ok`() = withConfiguredApp {
        val availableCarsJson = """
            {
              "name": "Breda"
            }
        """.trimIndent()

        val response = client.get("/location/search") {
            header("Authorization", "Bearer $authToken")
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(availableCarsJson)
        }
        assertEquals(HttpStatusCode.OK, response.status, "Search should return OK; body: ${response.bodyAsText()}")
    }

    @Test
    fun `search available cars within time window returns ok`() = withConfiguredApp {
        //TODO add query parameters for date
        val availableCarsJson = """
            {
              "name": "Breda"
            }
        """.trimIndent()

        val response = client.get("/location/search") {
            header("Authorization", "Bearer $authToken")
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(availableCarsJson)
        }
        assertEquals(HttpStatusCode.OK, response.status, "Search should return OK; body: ${response.bodyAsText()}")
    }

    @Test
    fun `book seeded vehicle returns ok`() = withConfiguredApp {
        val reservationJson = """
        {
          "vehicleId": "${vehicle.id}",
          "userName": "${user.username}"
        }
    """.trimIndent()

        val response = client.post("/reservation") {
            header("Authorization", "Bearer $authToken")
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(reservationJson)
        }

        assertEquals(
            HttpStatusCode.OK,
            response.status,
            "Booking happy path should return OK; body: ${response.bodyAsText()}"
        )
    }

    @Test
    fun `Send message to renter and owner returns ok`() = withConfiguredApp {
        //TODO send notification to renter and owner
        val notificationJson = """
        {
          "message": "Test notification from UC1",
          "userName": "${user.username}"
        }
    """.trimIndent()

        val response = client.post("/notification") {
            header("Authorization", "Bearer $authToken")
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(notificationJson)
        }
        assertEquals(
            HttpStatusCode.OK,
            response.status,
            "Notification should return OK; body: ${response.bodyAsText()}"
        )
    }
}