package com.example.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object ReviewTable : Table("reviews") {
    val id: Column<Long> = long("id").autoIncrement()
    val renterId: Column<Long> = long("renter_id").references(UserTable.id)
    val ownerId: Column<Long> = long("owner_id").references(UserTable.id)
    val rating: Column<ReviewRating?> = enumerationByName("rating", 1, ReviewRating::class).nullable()
    val comment: Column<String?> = varchar("comment", 65535).nullable()
    val reviewDate: Column<LocalDateTime?> = datetime("review_date").nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

@Serializable
data class Review(
    val id: Long = 0L,
    val renterId: Long,
    val ownerId: Long,
    val rating: ReviewRating? = null,
    val comment: String? = null,
    val reviewDate: LocalDateTime? = null,
)

@Serializable
enum class ReviewRating {
    @kotlinx.serialization.SerialName("1") ONE,
    @kotlinx.serialization.SerialName("2") TWO,
    @kotlinx.serialization.SerialName("3") THREE,
    @kotlinx.serialization.SerialName("4") FOUR,
    @kotlinx.serialization.SerialName("5") FIVE,
}
