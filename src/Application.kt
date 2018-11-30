package com.dmitriisalenko

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.auth.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import org.litote.kmongo.*

data class User(val name: String, val phone: String, val password: String)
data class Error(val message: String)

val client = KMongo.createClient()
val database = client.getDatabase("ktor-example-api")

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(Authentication) {
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        route("user") {
            post("/signup") {
                val user = call.receive<User>()
                val col = database.getCollection<User>()
                try {
                    col.save(user)
                    call.respond(user)
                }
                catch (e: Throwable) {
                    val error = Error(e.localizedMessage)
                    call.respond(HttpStatusCode.fromValue(501), error)
                }
            }
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }

        get("*") {
            call.respond(HttpStatusCode.NotFound, "Not found.")
        }
    }
}

