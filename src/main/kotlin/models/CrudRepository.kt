package com.example.models

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

interface CrudRepository<T, ID> {
    fun findAll(): List<T>
    fun findById(id: ID): T?
    fun create(entity: T)
    fun update(entity: T)
    fun delete(id: ID)
}

//
abstract class CrudDAO<T, ID, Table: org.jetbrains.exposed.v1.core.Table>(
    private val table: Table
): CrudRepository<T, ID>  {

    // Abstract functions to get and create an entity
    abstract fun getEntity(row: ResultRow): T
    protected abstract fun createEntity(entity: T, statement: UpdateBuilder<Int>)

    // Function to retrieve the id column safely
    private fun getTableId(table: Table): Column<ID> {
        val column = table.columns.firstOrNull { it.name == "id" }
            ?: throw IllegalArgumentException("Table does not have an 'id' column")

        // Perform safe casting (we're assuming this cast will succeed since it's based on the column name "id")
        @Suppress("UNCHECKED_CAST")
        return column as Column<ID>
    }

    // Private function to search a property in an object by its name
    private fun getObjectName(entity: T, type: String): Any {
        val idProperty = entity!!::class.members.firstOrNull { it.name == type }
            ?: throw IllegalArgumentException("Entity does not have an '$type' property")

        val idValue = idProperty.call(entity)
            ?: throw IllegalArgumentException("Entity '$type' property is null")

        @Suppress("UNCHECKED_CAST")
        return idValue
    }

    // Generic method to fetch all records from the database
    override fun findAll(): List<T> {
        var entity: List<T> = emptyList()
        transaction {
            entity = table.selectAll().map { row ->
                getEntity(row)
            }
        }

        return entity
    }

    // Generic method to find a record by its ID
    override fun findById(id: ID): T? =
        findAll().find { getObjectName(it, "id") == id }

    // Generic method to create a new entity
    override fun create(entity: T) {
        val objectId: ID = getObjectName(entity, "id") as? ID ?:
            throw IllegalArgumentException("Cannot update entity because entity ID is null or of incorrect type")

        findById(objectId) ?.let {
            throw Exception("Entity with ID $objectId already exists")
        }

        transaction {
            table.insert {
                createEntity(entity, it)
            }
        }

    }

    // Generic method to update an existing entity
    override fun update(entity: T) {
        val objectId: ID = getObjectName(entity, "id") as? ID ?:
            throw IllegalArgumentException("Cannot update entity because entity ID is null or of incorrect type")

        if (null == findById(objectId)) {
            throw Exception("Entity with ID $objectId does not exist")
        }

        transaction {
            table.update({ getTableId(table) eq objectId }) {
                createEntity(entity, it)
            }
        }
    }

    // Generic method to delete an entity by ID
    override fun delete(id: ID) {
        val tableId: Column<ID> = getTableId(table)
        transaction {
            table.deleteWhere { tableId eq id }
        }
    }

}