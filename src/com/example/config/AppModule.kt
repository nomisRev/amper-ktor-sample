package com.example.config

import com.example.comments.CommentRepository
import com.example.posts.PostRepository
import com.example.users.UserRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager

class AppModule(
    val userRepository: UserRepository,
    val postRepository: PostRepository,
    val commentRepository: CommentRepository
)

fun AppModule(application: Application): AppModule {
    val config = application.environment.config
    val dataSource = HikariDataSource(HikariConfig().apply {
        driverClassName = config.property("database.driverClassName").getString()
        jdbcUrl = config.property("database.jdbcUrl").getString()
        username = config.property("database.username").getString()
        password = config.property("database.password").getString()
        validate()
    })
    val database = Database.connect(dataSource).also { database ->
        application.monitor.subscribe(ApplicationStopped) {
            TransactionManager.closeAndUnregister(database)
            dataSource.close()
        }
    }
    return AppModule(
        UserRepository(database),
        PostRepository(database),
        CommentRepository(database)
    )
}
