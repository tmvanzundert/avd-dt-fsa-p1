package com.example.models

import java.util.UUID

interface CrudRepository<T, ID> {
    fun findAll(): List<T>
    fun findById(id: ID): T?
    fun create(item: T)
    fun update(item: T)
    fun delete(id: ID)
}