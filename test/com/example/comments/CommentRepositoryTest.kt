package org.jetbrains.ktor.sample.com.example.comments

import com.example.comments.CommentRepository
import com.example.comments.CommentTable
import com.example.comments.CommentWithPostAndUser
import com.example.comments.CommentWithUser
import com.example.com.example.users.User
import com.example.comments.Comment
import com.example.features.posts.domain.Post
import com.example.posts.PostRepository
import com.example.posts.PostTable
import com.example.users.UserRepository
import com.example.users.UserTable
import junit.framework.TestCase.assertFalse
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.ktor.sample.PostgresContainer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommentRepositoryTest {
    private val database = PostgresContainer.getDatabase()

    private val userRepository by lazy { UserRepository(database) }
    private val postRepository by lazy { PostRepository(database) }
    private val commentRepository by lazy { CommentRepository(database) }

    @Before
    fun setup() {
        transaction(database) { SchemaUtils.create(UserTable, PostTable, CommentTable) }
    }

    @After
    fun tearDown() {
        transaction(database) { SchemaUtils.drop(UserTable, PostTable, CommentTable) }
    }

    @Test
    fun `test comment repository returns null when comment not found`() {
        val comment = commentRepository.getById(Long.MAX_VALUE)
        assertNull(comment)
    }

    @Test
    fun `test comment repository returns null when comment with user not found`() {
        val comment = commentRepository.getByIdWithUser(Long.MAX_VALUE)
        assertNull(comment)
    }

    @Test
    fun `test comment repository returns null when comment with post and user not found`() {
        val comment = commentRepository.getByIdWithPostAndUser(Long.MAX_VALUE)
        assertNull(comment)
    }

    @Test
    fun create() {
        val (user, post) = createUniqueUserAndPost()
        val comment = commentRepository.create(post.id, user.id, "This is a test comment")
        assertEquals(Comment(comment.id, post.id, user.id, "This is a test comment", comment.createdAt), comment)
    }

    @Test
    fun getById() {
        val (user, post) = createUniqueUserAndPost()
        val id = commentRepository.create(post.id, user.id, "This is a test comment").id
        val comment = commentRepository.getById(id)!!
        assertEquals(Comment(id, post.id, user.id, "This is a test comment", comment.createdAt), comment)
    }

    @Test
    fun getByPostId() {
        val (user, post) = createUniqueUserAndPost()
        val comment = commentRepository.create(post.id, user.id, "This is a test comment")
        val comments = commentRepository.getByPostId(post.id)
        assertEquals(comment, comments.single())
    }

    @Test
    fun getByUserId() {
        val (user, post) = createUniqueUserAndPost()
        val comment1 = commentRepository.create(post.id, user.id, "This is a test comment")
        val comment2 = commentRepository.create(post.id, user.id, "This is a test comment")
        val comments = commentRepository.getByUserId(user.id)
        assertEquals(listOf(comment1, comment2), comments)
    }

    @Test
    fun getByPostIdWithUsers() {
        val (user, post) = createUniqueUserAndPost()
        val comment1 = commentRepository.create(post.id, user.id, "This is a test comment")
        val comment2 = commentRepository.create(post.id, user.id, "This is a test comment")
        val comments = commentRepository.getByPostIdWithUsers(post.id)
        assertEquals(listOf(comment1.withUser(user), comment2.withUser(user)), comments)
    }

    @Test
    fun getByIdWithUser() {
        val (user, post) = createUniqueUserAndPost()
        val comment = commentRepository.create(post.id, user.id, "This is a test comment")
        val commentWithUser = commentRepository.getByIdWithUser(comment.id)
        val expectedCommentWithUser = CommentWithUser(
            id = comment.id,
            postId = comment.postId,
            content = comment.content,
            createdAt = comment.createdAt,
            user = User(
                id = user.id,
                username = user.username,
                email = user.email,
                createdAt = user.createdAt
            )
        )

        assertEquals(expectedCommentWithUser, commentWithUser)
    }

    @Test
    fun getByIdWithPostAndUser() {
        val (user, post) = createUniqueUserAndPost()
        val comment = commentRepository.create(post.id, user.id, "This is a test comment")

        val commentWithPostAndUser = commentRepository.getByIdWithPostAndUser(comment.id)

        val expected = CommentWithPostAndUser(
            id = comment.id,
            content = comment.content,
            createdAt = comment.createdAt,
            post = Post(
                id = post.id,
                userId = post.userId,
                title = post.title,
                content = post.content,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt
            ),
            user = User(
                id = user.id,
                username = user.username,
                email = user.email,
                createdAt = user.createdAt
            )
        )

        assertEquals(expected, commentWithPostAndUser)
    }

    @Test
    fun delete() {
        val (user, post) = createUniqueUserAndPost()
        val comment = commentRepository.create(post.id, user.id, "This is a test comment")
        val deleted = commentRepository.delete(comment.id)
        assertTrue(deleted)
        assertNull(commentRepository.getById(comment.id))
    }

    @Test
    fun `delete non existing comment returns false`() {
        val deleted = commentRepository.delete(Long.MAX_VALUE)
        assertFalse(deleted)
    }

    @Test
    fun `test comment relationships`() {
        val user1 = userRepository.create("user1", "user1@example.com")
        val user2 = userRepository.create("user2", "user2@example.com")

        val post1 = postRepository.create(user1.id, "Post by User 1", "Content of post by user 1")
        val post2 = postRepository.create(user2.id, "Post by User 2", "Content of post by user 2")

        val comment1 = commentRepository.create(post1.id, user2.id, "Comment by User 2 on Post 1")
        val comment2 = commentRepository.create(post2.id, user1.id, "Comment by User 1 on Post 2")
        val comment3 = commentRepository.create(post1.id, user1.id, "Comment by User 1 on their own Post 1")

        val post1Comments = commentRepository.getByPostId(post1.id)
        assertEquals(2, post1Comments.size)
        assertTrue(post1Comments.any { it.id == comment1.id })
        assertTrue(post1Comments.any { it.id == comment3.id })

        val post2Comments = commentRepository.getByPostId(post2.id)
        assertEquals(1, post2Comments.size)
        assertEquals(comment2.id, post2Comments[0].id)

        val user1Comments = commentRepository.getByUserId(user1.id)
        assertEquals(2, user1Comments.size)
        assertTrue(user1Comments.any { it.id == comment2.id })
        assertTrue(user1Comments.any { it.id == comment3.id })

        val user2Comments = commentRepository.getByUserId(user2.id)
        assertEquals(1, user2Comments.size)
        assertEquals(comment1.id, user2Comments[0].id)

        val post1CommentsWithUsers = commentRepository.getByPostIdWithUsers(post1.id)
        assertEquals(2, post1CommentsWithUsers.size)
        val comment1WithUser = post1CommentsWithUsers.find { it.id == comment1.id }
        assertNotNull(comment1WithUser)

        val expectedComment1WithUser = CommentWithUser(
            id = comment1.id,
            postId = comment1.postId,
            content = comment1.content,
            createdAt = comment1.createdAt,
            user = User(
                id = user2.id,
                username = user2.username,
                email = user2.email,
                createdAt = user2.createdAt
            )
        )

        assertEquals(expectedComment1WithUser, comment1WithUser)

        val user1CommentsWithPosts = commentRepository.getByUserIdWithPosts(user1.id)
        assertEquals(2, user1CommentsWithPosts.size)
        val comment2WithPost = user1CommentsWithPosts.find { it.id == comment2.id }
        assertNotNull(comment2WithPost)

        val expectedComment2WithPost = CommentWithPostAndUser(
            id = comment2.id,
            content = comment2.content,
            createdAt = comment2.createdAt,
            post = Post(
                id = post2.id,
                userId = post2.userId,
                title = post2.title,
                content = post2.content,
                createdAt = post2.createdAt,
                updatedAt = post2.updatedAt
            ),
            user = User(
                id = user1.id,
                username = user1.username,
                email = user1.email,
                createdAt = user1.createdAt
            )
        )

        assertEquals(expectedComment2WithPost, comment2WithPost)
    }

    private fun createUniqueUserAndPost(): Pair<User, Post> {
        val randomSuffix = System.currentTimeMillis()
        val username = "uniqueUser$randomSuffix"
        val email = "uniqueUser$randomSuffix@example.com"

        val user = userRepository.create(username, email)
        val post = postRepository.create(user.id, "Unique Post", "This is a post by a unique user")

        return Pair(user, post)
    }
}
