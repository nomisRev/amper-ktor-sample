package org.jetbrains.ktor.sample

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.MapApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

object PostgresContainer {
    /**
     * At the end of the testsuite the Ryuk container started by Testcontainers will stop the
     * container.
     * https://java.testcontainers.org/test_framework_integration/manual_lifecycle_control/
     */
    private val container: PostgreSQLContainer<Nothing> by lazy {
        PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:16-alpine")).apply {
            waitingFor(Wait.forListeningPort())
            start()
        }
    }

    /**
     * Get a HikariDataSource connected to the PostgreSQL container
     */
    fun getDataSource(): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = container.driverClassName
            jdbcUrl = container.jdbcUrl
            username = container.username
            password = container.password
            maximumPoolSize = 2
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    /**
     * Get an Exposed Database instance connected to the PostgreSQL container
     */
    fun getDatabase(): Database {
        return Database.connect(getDataSource())
    }

    /**
     * Get a MapApplicationConfig with the PostgreSQL container connection details
     */
    fun getMapAppConfig() =
        MapApplicationConfig().apply {
            put("database.jdbcUrl", container.jdbcUrl)
            put("database.username", container.username)
            put("database.password", container.password)
            put("database.driverClassName", container.driverClassName)
            put("database.maxPoolSize", "2")
            put("database.cachePrepStmts", "true")
            put("database.prepStmtCacheSize", "250")
            put("database.prepStmtCacheSqlLimit", "2048")
            put("flyway.enabled", "true")
            put("flyway.locations", "classpath:db/migration")
            put("flyway.baselineOnMigrate", "true")
        }
}
