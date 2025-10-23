package com.example.models

import java.util.UUID

interface CrudRepository<T, ID> {
    suspend fun findAll(): List<T>
    suspend fun findById(id: ID): T?
    suspend fun create(item: T)
    suspend fun update(item: T)
    suspend fun delete(id: ID)
}