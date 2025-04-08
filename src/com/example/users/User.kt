package com.example.com.example.users

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val createdAt: Long
)
