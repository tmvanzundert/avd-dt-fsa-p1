package com.usecases

import com.BaseApplication
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

class UC1AutoHuren : BaseApplication() {

    @Test
    fun `search available cars within location proximity`() = withConfiguredApp {
        val availableCarsJson = """
            {
              "name": "Breda"
            }
        """.trimIndent()

        val response = client.get("/location/search/proximity") {
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
        val notificationJson = """
        {
          "message": "Test notification from UC1",
          "type": "INFO",
          "userId": 1
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