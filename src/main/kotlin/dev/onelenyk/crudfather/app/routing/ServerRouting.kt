package dev.onelenyk.crudfather.app.routing

import dev.onelenyk.crudfather.app.routing.controllers.DynamicController
import dev.onelenyk.crudfather.app.routing.controllers.ModelSchemeController
import dev.onelenyk.crudfather.domain.repository.DynamicRepository
import dev.onelenyk.crudfather.domain.repository.ModelSchemeRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.types.ObjectId

class ServerRouting(
    private val repository: ModelSchemeRepository,
    private val dynamicRepository: DynamicRepository,
) {
    private val modelSchemeController = ModelSchemeController(repository)
    private val dynamicController = DynamicController(repository, dynamicRepository)

    private val modelSchemeRoutes = ModelSchemeRoutes(modelSchemeController)
    private val dynamicRoutes = DynamicRoutes(dynamicController)
    private val utilRoutes = UtilRoutes()

    fun registerRoutes(routing: Routing) {
        modelSchemeRoutes.registerRoutes(routing)
        dynamicRoutes.registerRoutes(routing)
        utilRoutes.registerRoutes(routing)
    }
}
