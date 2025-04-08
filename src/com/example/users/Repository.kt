package com.example.users

import com.example.com.example.users.User
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object UserTable : LongIdTable("users", "user_id") {
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val createdAt = long("created_at")
}

class UserRepository(private val database: Database) {

    fun create(username: String, email: String): User = transaction(database) {
        val createdAt = System.currentTimeMillis()

        val userId = UserTable.insert {
            it[UserTable.username] = username
            it[UserTable.email] = email
            it[UserTable.createdAt] = createdAt
        } get UserTable.id

        User(userId.value, username, email, createdAt)
    }

    fun getById(id: Long): User? = transaction(database) {
        UserTable.selectAll()
            .where { UserTable.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    /**
     * Get all users
     */
    fun getAll(): List<User> = transaction(database) {
        UserTable.selectAll()
            .map { it.toUser() }
    }

    /**
     * Update a user
     */
    fun update(id: Long, username: String? = null, email: String? = null): Boolean = transaction(database) {
        UserTable.update({ UserTable.id eq id }) { stmt ->
            username?.let { stmt[UserTable.username] = it }
            email?.let { stmt[UserTable.email] = it }
        } > 0
    }

    fun delete(id: Long): Boolean = transaction(database) {
        UserTable.deleteWhere { UserTable.id eq id } > 0
    }

    private fun ResultRow.toUser(): User = User(
        id = this[UserTable.id].value,
        username = this[UserTable.username],
        email = this[UserTable.email],
        createdAt = this[UserTable.createdAt]
    )
}