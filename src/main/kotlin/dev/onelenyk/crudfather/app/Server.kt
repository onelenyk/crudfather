package dev.onelenyk.crudfather.app

import dev.onelenyk.crudfather.app.routing.ServerRouting
import dev.onelenyk.crudfather.di.koinModule
import dev.onelenyk.crudfather.di.provideServerPort
import io.github.cdimascio.dotenv.dotenv
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


class Server {
    fun start(): NettyApplicationEngine {
        val server = embeddedServer(
            Netty,
            port = provideServerPort(dotenv = dotenv()) ?: 8080
        ) {
            module(this)
        }
        server.start(wait = false)
        return server
    }

    fun module(application: Application) = application.apply {
        install(Koin) {
            slf4jLogger()
            modules(koinModule)
        }
        install(CallLogging)
        install(RequestValidation)
        configureSerialization()

        configureRouting()
    }

    private fun Application.configureSerialization() =
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }

    private fun Application.configureRouting() {
        val router: ServerRouting by inject()

        routing {
            router.registerRoutes(this)
        }
    }
}
