package com.example.comments

import com.example.features.posts.domain.Post
import com.example.com.example.users.User
import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: Long,
    val postId: Long,
    val userId: Long,
    val content: String,
    val createdAt: Long
) {
    fun withUser(user: User) = CommentWithUser(id, postId, content, createdAt, user)
}

@Serializable
data class CommentWithUser(
    val id: Long,
    val postId: Long,
    val content: String,
    val createdAt: Long,
    val user: User
)

@Serializable
data class CommentWithPostAndUser(
    val id: Long,
    val content: String,
    val createdAt: Long,
    val post: Post,
    val user: User
)
