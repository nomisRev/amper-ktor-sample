package com.example.features.comments.api

import com.example.comments.CommentRepository
import com.example.posts.PostsResource
import io.ktor.http.*
import io.ktor.resources.Resource
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/comments")
class CommentsResource {
    @Resource("{id}")
    class Id(val id: Int, val parent: CommentsResource = CommentsResource())
}

fun Application.commentRoutes(repo: CommentRepository) = routing {
    get<PostsResource.Comments> { route ->
        val comments = repo.getByPostIdWithUsers(route.parent.id)
        call.respond(comments)
    }

    post<PostsResource.Comments> { route ->
        val commentParams = call.receive<Map<String, String>>()
        val userId = commentParams["userId"]?.toLongOrNull() ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            "Valid userId is required"
        )
        val content = commentParams["content"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            "Content is required"
        )

        val comment = repo.create(route.parent.id, userId, content)
        call.respond(HttpStatusCode.Created, comment)
    }
}
