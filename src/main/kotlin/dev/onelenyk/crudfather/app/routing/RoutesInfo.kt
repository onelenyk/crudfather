package dev.onelenyk.crudfather.app.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Routing.routesInfo() {
    get("/routes") {
        val routesList = this@routesInfo.getAllRoutes()
        call.respond(routesList)
    }
    staticResources("/doc", "dokka")
    staticResources("/", "dokka")
    get("/live") {
        call.respond(HttpStatusCode.OK)
    }
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
