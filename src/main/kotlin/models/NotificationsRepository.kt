package com.example.models

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import kotlin.time.ExperimentalTime

interface NotificationRepository : CrudRepository<Notification, Long> {

    fun getNotificationsForUser(userId: Long): List<Notification>

    @OptIn(ExperimentalTime::class)
    fun createNotification(userId: Long, type: String, message: String, timestamp: LocalDateTime = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC), ): Notification

    fun markAsRead(notificationId: Long): Notification

    fun markAllAsReadForUser(userId: Long)
}

class NotificationDao :
    CrudDAO<Notification, Long, NotificationTable>(NotificationTable),
    NotificationRepository {

    override fun getEntity(row: ResultRow): Notification {
        return Notification(
            id = row[NotificationTable.id],
            user_id = row[NotificationTable.user_id],
            type = row[NotificationTable.type],
            message = row[NotificationTable.message],
            timestamp = row[NotificationTable.timestamp],
            is_read = row[NotificationTable.is_read],
        )
    }

    override fun createEntity(entity: Notification, statement: UpdateBuilder<Int>) {
        statement[NotificationTable.id] = entity.id
        statement[NotificationTable.user_id] = entity.user_id
        statement[NotificationTable.type] = entity.type
        statement[NotificationTable.message] = entity.message
        statement[NotificationTable.timestamp] = entity.timestamp
        statement[NotificationTable.is_read] = entity.is_read
    }

    override fun getNotificationsForUser(userId: Long): List<Notification> {
        return findAll()
            .filter { it.user_id == userId }
            .sortedByDescending { it.timestamp }
    }

    override fun createNotification(
        userId: Long,
        type: String,
        message: String,
        timestamp: LocalDateTime
    ): Notification {
        val notification = Notification(
            id = 0L,
            user_id = userId,
            type = type,
            message = message,
            timestamp = timestamp,
            is_read = false,
        )
        create(notification)
//        Onderstaand zal als landmijn crashen als er meerdere gebruikers notificaties aanmaken, maar ik kan voor nu even niet teveel aanpassen aan CrudDAO Create.
        val inserted = findAll().maxByOrNull { it.id }!!
        return inserted
    }

    override fun markAsRead(notificationId: Long): Notification {
        val notIf = findById(notificationId) ?: throw Exception("Notification not found")
        updateProperty(notificationId, "is_read", true)
        return notIf.copy(is_read = true)
    }

    override fun markAllAsReadForUser(userId: Long) {
        getNotificationsForUser(userId).forEach { notif ->
            if (!notif.is_read) {
                updateProperty(notif.id, "is_read", true)
            }
        }
    }
}
