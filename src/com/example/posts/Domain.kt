package com.example.features.posts.domain

import com.example.com.example.users.User
import kotlinx.serialization.Serializable

/**
 * Data class representing a Post entity
 */
@Serializable
data class Post(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long?
)

/**
 * Data class representing a Post with User information
 */
@Serializable
data class PostWithUser(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long?,
    val user: User
)