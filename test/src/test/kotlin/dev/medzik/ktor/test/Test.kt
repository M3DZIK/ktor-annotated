package dev.medzik.ktor.test

import dev.medzik.ktor.annotated.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ExampleTest {
    @Test
    fun testController() = testApplication {
        application {
            configureRouting()
        }

        val client = createClient {}
        assertEquals(
            "Test",
            client.get("/api/test")
                .bodyAsText()
        )
        assertEquals(
            "Test",
            client.post("/api/test")
                .bodyAsText()
        )
        assertEquals(
            "Second test",
            client.post("/api/secondTest")
                .bodyAsText()
        )
    }
}
