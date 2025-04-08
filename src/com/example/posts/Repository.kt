package com.example.posts

import com.example.features.posts.domain.Post
import com.example.features.posts.domain.PostWithUser
import com.example.com.example.users.User
import com.example.users.UserTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PostTable : LongIdTable("posts", "post_id") {
    val userId = long("user_id").references(UserTable.id)
    val title = varchar("title", 100)
    val content = text("content")

    // TODO: KotlinX Instant with defaultExpression
    val createdAt = long("created_at")
    val updatedAt = long("updated_at").nullable()
}

class PostRepository(private val database: Database) {
    fun create(userId: Long, title: String, content: String): Post = transaction(database) {
        val now = System.currentTimeMillis()

        val postId = PostTable.insert {
            it[PostTable.userId] = userId
            it[PostTable.title] = title
            it[PostTable.content] = content
            it[PostTable.createdAt] = now
            it[PostTable.updatedAt] = null
        } get PostTable.id

        Post(postId.value, userId, title, content, now, null)
    }

    fun getById(id: Long): Post? = transaction(database) {
        PostTable
            .selectAll()
            .where { PostTable.id eq id }
            .map { it.toPost() }
            .singleOrNull()
    }

    fun getByIdWithUser(id: Long): PostWithUser? = transaction(database) {
        PostTable.innerJoin(UserTable)
            .selectAll()
            .where { PostTable.id eq id }
            .map { it.toPostWithUser() }
            .singleOrNull()
    }

    fun getAll(): List<Post> = transaction(database) {
        PostTable.selectAll()
            .map { it.toPost() }
    }

    fun getAllWithUsers(): List<PostWithUser> = transaction(database) {
        PostTable.innerJoin(UserTable)
            .selectAll()
            .map { it.toPostWithUser() }
    }

    fun getByUserId(userId: Long): List<Post> = transaction(database) {
        PostTable
            .selectAll()
            .where { PostTable.userId eq userId }
            .map { it.toPost() }
    }

    fun update(id: Long, title: String? = null, content: String? = null): Boolean = transaction(database) {
        val now = System.currentTimeMillis()
        PostTable.update({ PostTable.id eq id }) { stmt ->
            title?.let { stmt[PostTable.title] = it }
            content?.let { stmt[PostTable.content] = it }
            stmt[PostTable.updatedAt] = now
        } > 0
    }

    fun delete(id: Long): Boolean = transaction(database) {
        PostTable.deleteWhere { PostTable.id eq id } > 0
    }

    private fun ResultRow.toPost(): Post = Post(
        id = this[PostTable.id].value,
        userId = this[PostTable.userId],
        title = this[PostTable.title],
        content = this[PostTable.content],
        createdAt = this[PostTable.createdAt],
        updatedAt = this[PostTable.updatedAt]
    )

    private fun ResultRow.toPostWithUser(): PostWithUser = PostWithUser(
        id = this[PostTable.id].value,
        title = this[PostTable.title],
        content = this[PostTable.content],
        createdAt = this[PostTable.createdAt],
        updatedAt = this[PostTable.updatedAt],
        user = User(
            id = this[UserTable.id].value,
            username = this[UserTable.username],
            email = this[UserTable.email],
            createdAt = this[UserTable.createdAt]
        )
    )
}