package com.example.models

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object ReviewTable: Table("Review") {
    val id: Column<Long> = long("id").autoIncrement()
    val user_id: Column<Long> = long("user_id")
    val rating: Column<Long> = long("rating")
    val comment: Column<String> = varchar("comment", 255)
    val review_date: Column<LocalDateTime> = datetime("review_date")
    val target_type: Column<String> = varchar("target_type", 255)
    val target_id: Column<Long> = long("target_id")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

data class Review(
    val id: Long,
    val user_id: Long,
    val rating: Int,
    val comment: String,
    val review_date: LocalDateTime,
    val target_type: String,
    val target_id: Long
)