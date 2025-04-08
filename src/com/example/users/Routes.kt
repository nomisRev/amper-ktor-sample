package com.example.users

import io.ktor.http.*
import io.ktor.resources.Resource
import io.ktor.server.application.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/users")
class UsersResource {
    @Resource("{id}")
    class Id(val parent: UsersResource = UsersResource(), val id: Long)

    @Resource("/")
    class Create(
        val parent: UsersResource = UsersResource(),
        val username: String,
        val email: String
    )

    @Resource("posts")
    class Posts(val parent: Id)
}

fun Application.userRoutes(userRepository: UserRepository) = routing {
    get<UsersResource> {
        call.respond(userRepository.getAll())
    }

    get<UsersResource.Id> { route ->
        val user = userRepository.getById(route.id)
        if (user != null) call.respond(user)
        else call.respond(HttpStatusCode.NotFound, "User not found")
    }

    post<UsersResource.Create> { create ->
        val user = userRepository.create(create.username, create.email)
        call.respond(HttpStatusCode.Created, user)
    }
}
