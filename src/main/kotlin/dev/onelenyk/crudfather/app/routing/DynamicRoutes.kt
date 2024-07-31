package dev.onelenyk.crudfather.app.routing

import dev.onelenyk.crudfather.app.routing.controllers.DynamicController
import io.ktor.server.application.*
import io.ktor.server.routing.*

class DynamicRoutes(private val dynamicController: DynamicController) {
    fun registerRoutes(routing: Routing) {
        routing.route("dynamic/{model}") {
            post {
                return@post dynamicController.create(call = call)
            }
            get {
                return@get  dynamicController.readAll(call = call)
            }
            get("/{id}") {
                return@get  dynamicController.getById(call = call)
            }
            put("/{id}") {
                return@put  dynamicController.update(call = call)
            }
            delete("/{id}") {
                return@delete  dynamicController.delete(call = call)
            }

        }
    }
}
