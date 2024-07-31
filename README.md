# Annotation-based routing and features for Ktor

**ktor-annotated** simplifies Ktor development by providing a declarative way to define routes
and handle HTTP requests using annotations. This library eliminates the need for verbose
routing configurations and improves code readability.

## Example code

```kotlin
@Controller
class ExampleController {
    @GET("/example")
    suspend fun example(call: ApplicationCall) {
        call.respond("Test")
    }
}

@Controller("/api")
class ApiController {
    @GET("/test")
    suspend fun example(call: ApplicationCall) {
        call.respond("Test API controller")
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = { configureRouting() })
        .start(wait = true)
}
```
