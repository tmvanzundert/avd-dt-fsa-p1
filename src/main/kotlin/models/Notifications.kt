package com.example.models

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object NotificationTable: Table("notification") {
    val id: Column<Long> = long("id").autoIncrement()
    val user_id: Column<Long> = long("user_id").references(UserTable.id).uniqueIndex()
    val type: Column<String> = varchar("type", 40)
    val timestamp: Column<LocalDateTime> = datetime("timestamp")
    val is_read: Column<Boolean> = bool("is_read")

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

data class Notification(
    val id: Long,
    val user_id: Long,
    val type: String,
    val message: String,
    val timestamp: LocalDateTime,
    val is_read: Boolean
)