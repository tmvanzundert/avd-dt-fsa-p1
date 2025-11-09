package com.usecases

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

// bad flows
// No cars available for the selected criteria
// Invalid payment details
// Owner doesn't confirm or cancels the booking

class UC1AutoHuren : BaseApplication() {

    @Test
    fun `search available cars returns ok`() = withConfiguredApp {
        //TODO add query parameters for date, location, car type
        val response = client.get("/vehicle") {
            header("Authorization", "Bearer $authToken")
            accept(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status, "Search should return OK; body: ${response.bodyAsText()}")
    }

    @Test
    fun `book seeded vehicle returns ok`() = withConfiguredApp {
        val reservationJson = """
        {
          "vehicleId": ${vehicle.id},
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

    // Bad Flows
    @Test
    fun `search available cars returns no content when none available`() = withConfiguredApp {
        //TODO set up criteria that yield no available cars

        val response = client.get("/vehicle/available") {
            header("Authorization", "Bearer $authToken")
            accept(ContentType.Application.Json)
        }
        assertEquals(
            HttpStatusCode.NoContent,
            response.status,
            "Search with no available cars should return No Content; body: ${response.bodyAsText()}"
        )
    }

    @Test
    fun `book vehicle with invalid payment details returns bad request`() = withConfiguredApp {
        //TODO book the vehicle with invalid payment details
        val invalidReservationJson = """
        {
          "vehicleId": ${vehicle.id},
          "userName": "${user.username}"
        }
        """.trimIndent()

        val response = client.post("/reservation") {
            header("Authorization", "Bearer $authToken")
            accept(ContentType.Application.Json)
            setBody(invalidReservationJson)
        }
        assertEquals(
            HttpStatusCode.BadRequest,
            response.status,
            "Booking with invalid payment details should return Bad Request; body: ${response.bodyAsText()}"
        )
    }

    @Test
    fun `owner cancels booking returns conflict`() = withConfiguredApp {
//        val cancelBookingJson = """
//            {}
//        """.trimIndent()
        val response = client.post("/reservation/cancel") {
            header("Authorization", "Bearer $authToken")
            accept(ContentType.Application.Json)
//            setBody(cancelBookingJson)
        }
        assertEquals(
            HttpStatusCode.Conflict,
            response.status,
            "Owner cancelling booking should return Conflict; body: ${response.bodyAsText()}"
        )
    }
}