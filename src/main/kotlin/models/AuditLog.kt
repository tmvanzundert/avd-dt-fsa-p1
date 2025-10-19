package com.example.models

import org.jetbrains.exposed.v1.core.Table

object AuditLogTable: Table("AuditLog") {
    val id: Long = long("id").autoIncrement().primaryKey()
    val entity: String = varchar("entity", 100)
    val entityId: Long = long("entityId")
    val action: AuditAction = enumerationByName("action", 20, AuditAction::class).default(AuditAction.READ)
    val payload: String = text("payload")
    val createdAt: String = varchar("createdAt", 50)
    val createdBy: String = varchar("createdBy", 100)
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