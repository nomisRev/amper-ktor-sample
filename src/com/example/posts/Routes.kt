package com.example.posts

import com.example.users.UsersResource
import io.ktor.http.*
import io.ktor.resources.Resource
import io.ktor.server.application.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/posts")
class PostsResource {
    @Resource("{id}")
    class Id(val parent: PostsResource = PostsResource(), val id: Long)

    @Resource("/")
    class Create(
        val userId: Long,
        val title: String,
        val content: String,
        val parent: UsersResource = UsersResource(),
    )

    @Resource("{id}/comments")
    class Comments(val parent: Id)
}

fun Application.postRoutes(postRepository: PostRepository) = routing {
    get<PostsResource> {
        call.respond(postRepository.getAllWithUsers())
    }

    get<PostsResource.Id> { route ->
        val post = postRepository.getByIdWithUser(route.id)
        if (post != null) {
            call.respond(post)
        } else {
            call.respond(HttpStatusCode.NotFound, "Post not found")
        }
    }

    get<UsersResource.Posts> { route ->
        val posts = postRepository.getByUserId(route.parent.id)
        call.respond(posts)
    }

    post<PostsResource.Create> { create ->
        val post = postRepository.create(create.userId, create.title, create.content)
        call.respond(HttpStatusCode.Created, post)
    }
}
