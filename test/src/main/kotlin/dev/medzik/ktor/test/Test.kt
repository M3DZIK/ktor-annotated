package dev.medzik.ktor.test

import dev.medzik.ktor.annotated.configureRouting
import dev.medzik.ktor.annotations.Controller
import dev.medzik.ktor.annotations.GET
import dev.medzik.ktor.annotations.POST
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*

@Controller
class Test {
    @GET("/test")
    @POST("/test")
    suspend fun test(call: ApplicationCall) {
        call.respond("Test")
    }

    @POST("/secondTest")
    fun secondTest() {
        println("Test")
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}
