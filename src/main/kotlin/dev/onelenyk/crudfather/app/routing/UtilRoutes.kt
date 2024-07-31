package dev.onelenyk.crudfather.app.routing

import dev.onelenyk.crudfather.app.routing.controllers.DynamicController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


class UtilRoutes {
    fun registerRoutes(routing: Routing) {
        routing.get("/routes") {
            val routesList = routing.getAllRoutes()
            call.respond(routesList)
        }
        routing.get("/live") {
            call.respond(HttpStatusCode.OK)
        }
        routing.staticResources("/", "dokka")
    }

    private fun Route.getAllRoutes(): List<String> {
        val routesList = mutableListOf<String>()
        this.children.forEach { route ->
            route.toRouteList(routesList)
        }
        return routesList
    }

    private fun Route.toRouteList(
        routesList: MutableList<String>,
        parentPath: String = "",
    ) {
        val currentPath =
            if (this.selector is RootRouteSelector) {
                parentPath
            } else {
                val segment = this.selector.toString()
                if (parentPath.isEmpty()) segment else "$parentPath/$segment"
            }

        if (this.children.isEmpty()) {
            routesList.add(currentPath)
        } else {
            this.children.forEach { child ->
                child.toRouteList(routesList, currentPath)
            }
        }
    }
}
