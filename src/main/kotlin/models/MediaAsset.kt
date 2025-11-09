package com.example.models

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.datetime.datetime


object MediaAssetTable : Table("VehicleStatus") {
    val id: Column<Long> = long("id").autoIncrement()
    val owner_type: Column<String> = varchar("owner_type", 40)
    val owner_id: Column<Long> = long("owner_id")
    val kind: Column<String> = varchar("kind", 40)
    val uri: Column<String> = varchar("uri", 255)
    val checksum: Column<String> = varchar("checksum", 128)
    val created_at: Column<LocalDateTime> = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}

data class MediaAsset(
    val id: Long,
    val owner_type: String,
    val owner_id: Long,
    val kind: String,
    val uri: String,
    val checksum: String,
    val created_at: LocalDateTime,
)