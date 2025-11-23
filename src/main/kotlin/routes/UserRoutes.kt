package com.example.routes

import com.example.models.User
import com.example.models.UserDao
import io.ktor.server.routing.*
import kotlin.reflect.KClass

class UserRoute(entityClass: KClass<User>, override val dao: UserDao) : ModelRoute<UserDao, User>("user", entityClass)

fun Route.userRoutes(userDao: UserDao) {
    val userRoute = UserRoute(User::class, userDao)

    userRoute.apply {
        list()
        getById()
        create()
        update()
        delete()
    }
}