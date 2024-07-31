package dev.onelenyk.crudfather.app.routing

import dev.onelenyk.crudfather.app.routing.controllers.ModelSchemeController
import io.ktor.server.application.*
import io.ktor.server.routing.*

class ModelSchemeRoutes(private val controller: ModelSchemeController) {
    fun registerRoutes(routing: Routing) {
        routing.route("/models") {
            post {
                return@post controller.create(call)
            }
            get {
                return@get controller.readAll(call)
            }
            get("/{id}") {
                return@get controller.getById(call)
            }
            put("/{id}") {
                return@put controller.update(call)
            }
            delete("/{id}") {
                return@delete controller.delete(call)
            }
        }
    }
}
