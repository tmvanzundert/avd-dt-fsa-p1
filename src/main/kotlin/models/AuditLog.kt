package com.example.models

import org.jetbrains.exposed.v1.core.*

object AuditLogTable: Table("AuditLog") {
    val id: Column<Long> = long("id")/*.autoIncrement()*/
    val entity: Column<String> = varchar("entity", 100)
    val entityId: Column<Long> = long("entityId")
    val action: Column<AuditAction> = enumerationByName("action", 20, AuditAction::class).default(AuditAction.READ)
    val payload: Column<String> = text("payload")
    val createdAt: Column<String> = varchar("createdAt", 50)
    val createdBy: Column<String> = varchar("createdBy", 100)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

data class AuditLog constructor(
    val id: Long = 0L,
    val entity: String,
    val entityId: Long,
    val action: AuditAction = AuditAction.READ,
    val payload: String,
    val createdAt: String,
    val createdBy: String
)

enum class AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    READ
}