package com.example.users

import com.example.com.example.users.User
import com.example.comments.CommentTable
import com.example.posts.PostTable
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.ktor.sample.PostgresContainer
import org.jetbrains.ktor.sample.com.example.withApp
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserRoutesTest {

    private val database = PostgresContainer.getDatabase()
    private val userRepository by lazy { UserRepository(database) }
    private val testUser by lazy { userRepository.create("testuser", "test@example.com") }

    @Before
    fun setup() {
        transaction(database) { SchemaUtils.create(UserTable, PostTable, CommentTable) }
        testUser // create user
    }

    @After
    fun tearDown() {
        transaction(database) { SchemaUtils.drop(CommentTable, PostTable, UserTable) }
    }

    @Test
    fun `test GET users returns all users`() = withApp {
        val response = get("/users")

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.body<List<User>>(), testUser)
    }

    @Test
    fun `test GET user by id returns user when found`() = withApp {
        val response = get("/users/${testUser.id}")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(testUser, response.body<User>())
    }

    @Test
    fun `test GET user by id returns 404 when not found`() = withApp {
        val response = get("/users/999")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("User not found"))
    }

    @Test
    fun `test POST user creates new user`() = withApp {
        val response = post("/users/?username=newuser&email=new@example.com")

        assertEquals(HttpStatusCode.Created, response.status)
        val user = response.body<User>()
        assertEquals("newuser", user.username)
        assertEquals("new@example.com", user.email)
    }
}
