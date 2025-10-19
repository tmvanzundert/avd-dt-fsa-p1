package com.example.models

import java.io.File

interface AuditLogRepository: CrudRepository<AuditLog, Long> {
    suspend fun export(filePath: String)
}

abstract class AuditLogDao: AuditLogRepository {

    override suspend fun findAll(): List<AuditLog> {
        var auditLogs = emptyList<AuditLog>()
        transaction {
            auditLogs = AuditLogTable.selectAll().map {
                AuditLog(
                    id = it[AuditLogTable.id],
                    entity = it[AuditLogTable.entity],
                    entityId = it[AuditLogTable.entityId],
                    action = it[AuditLogTable.action],
                    payload = it[AuditLogTable.payload],
                    createdAt = it[AuditLogTable.createdAt],
                    createdBy = it[AuditLogTable.createdBy]
                )
            }
        }

        return auditLogs
    }

    override suspend fun findById(id: Long): AuditLog? {
        var auditLog: AuditLog? = null
        transaction {
            val result = AuditLogTable.select { AuditLogTable.id eq id }.singleOrNull()
            if (result != null) {
                auditLog = AuditLog(
                    id = result[AuditLogTable.id],
                    entity = result[AuditLogTable.entity],
                    entityId = result[AuditLogTable.entityId],
                    action = result[AuditLogTable.action],
                    payload = result[AuditLogTable.payload],
                    createdAt = result[AuditLogTable.createdAt],
                    createdBy = result[AuditLogTable.createdBy]
                )
            }
        }

        return auditLog ?: throw Exception("AuditLog not found")
    }

    override suspend fun create(entity: AuditLog) {
        var newAuditLog: AuditLog? = null
        transaction {
            newAuditLog = AuditLogTable.insert {
                it[entity] = entity.entity
                it[entityId] = entity.entityId
                it[action] = entity.action
                it[payload] = entity.payload
                it[createdAt] = entity.createdAt
                it[createdBy] = entity.createdBy
            }
        }

        if (newAuditLog == null) {
            throw Exception("Failed to create AuditLog")
        }
    }

    override suspend fun update(entity: AuditLog) {
        val auditLogId = findById(entity.id)?.id ?: throw Exception("AuditLog not found")
        transaction {
            AuditLogTable.update({ AuditLogTable.id eq auditLogId }) {
                it[entity] = entity.entity
                it[entityId] = entity.entityId
                it[action] = entity.action
                it[payload] = entity.payload
                it[createdAt] = entity.createdAt
                it[createdBy] = entity.createdBy
            }
        }
    }

    override suspend fun delete(id: Long) {
        val auditLogId = findById(id)?.id ?: throw Exception("AuditLog not found")
        transaction {
            val deleteAuditLog = AuditLogTable.deleteWhere { AuditLogTable.id eq auditLogId }
        }

        if (deleteAuditLog == 0) {
            throw Exception("Failed to delete AuditLog")
        }
    }

    override suspend fun export(filePath: String) {
        val auditLog = findAll()
        if (auditLog.isEmpty()) {
            throw Exception("No AuditLogs to export")
        }

        val file = File(filePath)
        file.writeText("""
            AuditLog Export
            ----------------
            ID: ${auditLog.id}
            Entity: ${auditLog.entity}
            Entity ID: ${auditLog.entityId}
            Action: ${auditLog.action}
            Payload: ${auditLog.payload}
            Created At: ${auditLog.createdAt}
            Created By: ${auditLog.createdBy}
        """.trimIndent())
    }
}