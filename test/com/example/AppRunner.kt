package org.jetbrains.ktor.sample.com.example

import com.example.module
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.*
import org.jetbrains.ktor.sample.PostgresContainer

fun withApp(block: suspend HttpClient.() -> Unit) = testApplication {
    environment { config = PostgresContainer.getMapAppConfig() }
    application { module() }
    val client = createClient {
        install(Resources)
        install(ContentNegotiation) { json() }
    }
    block(client)
}