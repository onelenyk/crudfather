package dev.onelenyk.crudfather

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

//   port = System.getenv("CUSTOM_PORT")?.toInt() ?: 8080) {

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(CallLogging)
        install(ContentNegotiation) {
            json() // Use the appropriate JSON serialization library configuration here
        }
        routing {

        }
    }.start(wait = true)
}
