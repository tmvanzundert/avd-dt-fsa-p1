package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class RentalCar (
    val id: Long = 0L,
    val brand: String,
    val type: String,
    val color: String,
    val range: Int,
    val licensePlateNumber: String,
    val available: Status = Status.AVAILABLE
)

enum class Status {
    AVAILABLE,
    RENTED,
    MAINTENANCE
}