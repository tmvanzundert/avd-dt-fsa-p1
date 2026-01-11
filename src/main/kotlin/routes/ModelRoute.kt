package com.example.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.reflect.KClass
import kotlin.reflect.full.*

interface RouteRepository<Entity : Any> {
    fun Route.list()
    fun Route.getById()
    fun Route.create()
    fun Route.update()
    fun Route.delete()
}

abstract class ModelRoute<Dao, Entity : Any>(
    path: String,
    private val entityClass: KClass<Entity>
    
) : RouteRepository<Entity> {
    abstract val dao: Dao
    private val name = path.lowercase()
    private val nameCap = name.replaceFirstChar { it.uppercase() }

    private fun executeDaoFun(dao: Dao, funName: String, argument: Any? = null): Any? {
        val function = dao!!::class.memberFunctions.singleOrNull() { it.name == funName }
            ?: throw IllegalStateException("$funName function does not exist")

        val args = if (argument != null) arrayOf(dao, argument) else arrayOf(dao)
        return function.call(*args)
    }

    // Private function to search a property in an object by its name
    private fun getObjectName(entity: Any, type: String): Any? {
        val property = entity::class.memberProperties.firstOrNull { it.name == type }
            ?: throw IllegalArgumentException("Entity does not have an '$type' property")

        return property.call(entity)
    }

    override fun Route.list() {
        get("/$name") {
            val find = executeDaoFun(dao, "findAll")
            call.respond(find as Any)
        }
    }

    override fun Route.getById() {
        get("/$name/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid $name ID")
                return@get
            }

            val entityObject = executeDaoFun(dao, "findById", id)
            if (entityObject == null) {
                call.respond(HttpStatusCode.NotFound, "$nameCap not found")
            } else {
                call.respond(entityObject)
            }
        }
    }

    override fun Route.create() {
        post("/$name") {
            val entityObject = call.receive(entityClass)
            try {
                executeDaoFun(dao, "create", entityObject)
                call.respond(HttpStatusCode.Created, entityObject.toString())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, e.message ?: "$nameCap already exists")
            }
        }
    }

    override fun Route.update() {
        put("/$name/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid $name ID")
                return@put
            }

            val entityObject = call.receive(entityClass)
            if (getObjectName(entityObject, "id") != id) {
                call.respond(HttpStatusCode.BadRequest, "$nameCap ID mismatch")
                return@put
            }
            try {
                executeDaoFun(dao, "update", entityObject)
                call.respond(HttpStatusCode.OK, entityObject.toString())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "$nameCap not found")
            }
        }
    }

    override fun Route.delete() {
        delete("/$name/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid $name ID")
                return@delete
            }

            try {
                executeDaoFun(dao, "delete", id)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "$nameCap not found")
            }
        }
    }

}