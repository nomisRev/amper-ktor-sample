package com.example.comments

import com.example.features.posts.domain.Post
import com.example.com.example.users.User
import com.example.posts.PostTable
import com.example.users.UserTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object CommentTable : LongIdTable("comments", "comment_id") {
    val postId = long("post_id").references(PostTable.id)
    val userId = long("user_id").references(UserTable.id)
    val content = text("content")
    val createdAt = long("created_at")
}

class CommentRepository(private val database: Database) {

    fun create(postId: Long, userId: Long, content: String): Comment = transaction(database) {
        val now = System.currentTimeMillis()

        val commentId = CommentTable.insert {
            it[CommentTable.postId] = postId
            it[CommentTable.userId] = userId
            it[CommentTable.content] = content
            it[CommentTable.createdAt] = now
        } get CommentTable.id

        Comment(commentId.value, postId, userId, content, now)
    }

    fun getById(id: Long): Comment? = transaction(database) {
        CommentTable.selectAll()
            .where { CommentTable.id eq id }
            .map { it.toComment() }
            .singleOrNull()
    }

    fun getByIdWithUser(id: Long): CommentWithUser? = transaction(database) {
        (CommentTable innerJoin UserTable)
            .selectAll()
            .where { CommentTable.id eq id }
            .map { it.toCommentWithUser() }
            .singleOrNull()
    }

    fun getByIdWithPostAndUser(id: Long): CommentWithPostAndUser? = transaction(database) {
        CommentTable
            .innerJoin(PostTable) { CommentTable.postId eq PostTable.id }
            .innerJoin(UserTable) { CommentTable.userId eq UserTable.id }
            .selectAll()
            .where { CommentTable.id eq id }
            .map { it.toCommentWithPostAndUser() }
            .singleOrNull()
    }

    fun getByPostId(postId: Long): List<Comment> = transaction(database) {
        CommentTable.selectAll()
            .where { CommentTable.postId eq postId }
            .orderBy(CommentTable.id)
            .map { it.toComment() }
    }

    fun getByPostIdWithUsers(postId: Long): List<CommentWithUser> = transaction(database) {
        CommentTable.innerJoin(UserTable)
            .selectAll()
            .where { CommentTable.postId eq postId }
            .orderBy(CommentTable.id)
            .map { it.toCommentWithUser() }
    }

    fun getByUserId(userId: Long): List<Comment> = transaction(database) {
        CommentTable.selectAll()
            .where { CommentTable.userId eq userId }
            .orderBy(CommentTable.id)
            .map { it.toComment() }
    }

    fun getByUserIdWithPosts(userId: Long): List<CommentWithPostAndUser> = transaction(database) {
        CommentTable
            .innerJoin(PostTable) { CommentTable.postId eq PostTable.id }
            .innerJoin(UserTable) { CommentTable.userId eq UserTable.id }
            .selectAll()
            .where { CommentTable.userId eq userId }
            .orderBy(CommentTable.id)
            .map { it.toCommentWithPostAndUser() }
    }

    fun delete(id: Long): Boolean = transaction(database) {
        CommentTable.deleteWhere { CommentTable.id eq id } > 0
    }

    private fun ResultRow.toComment() = Comment(
        id = this[CommentTable.id].value,
        postId = this[CommentTable.postId],
        userId = this[CommentTable.userId],
        content = this[CommentTable.content],
        createdAt = this[CommentTable.createdAt]
    )

    private fun ResultRow.toCommentWithUser() = CommentWithUser(
        id = this[CommentTable.id].value,
        postId = this[CommentTable.postId],
        content = this[CommentTable.content],
        createdAt = this[CommentTable.createdAt],
        user = User(
            id = this[UserTable.id].value,
            username = this[UserTable.username],
            email = this[UserTable.email],
            createdAt = this[UserTable.createdAt]
        )
    )

    private fun ResultRow.toCommentWithPostAndUser() = CommentWithPostAndUser(
        id = this[CommentTable.id].value,
        content = this[CommentTable.content],
        createdAt = this[CommentTable.createdAt],
        post = Post(
            id = this[PostTable.id].value,
            userId = this[PostTable.userId],
            title = this[PostTable.title],
            content = this[PostTable.content],
            createdAt = this[PostTable.createdAt],
            updatedAt = this[PostTable.updatedAt]
        ),
        user = User(
            id = this[UserTable.id].value,
            username = this[UserTable.username],
            email = this[UserTable.email],
            createdAt = this[UserTable.createdAt]
        )
    )
}
