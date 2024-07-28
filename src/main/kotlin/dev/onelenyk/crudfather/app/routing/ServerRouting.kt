package dev.onelenyk.crudfather.app.routing

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
    private val modelSchemeRoutes = ModelSchemeRoutes(repository)
    private val dynamicRoutes = DynamicRoutes(repository, dynamicRepository)

    fun registerRoutes(routing: Routing) {
        modelSchemeRoutes.registerRoutes(routing)
        dynamicRoutes.registerRoutes(routing)
        routing.routesInfo()
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.checkModelExists(
    repository: ModelSchemeRepository,
    modelName: String,
): Boolean {
    if (modelName.isBlank()) {
        call.respond(HttpStatusCode.BadRequest, "Model name is required")
        return false
    }

    val modelExists = runBlocking { repository.modelExistsByName(modelName) }
    if (!modelExists) {
        call.respond(HttpStatusCode.NotFound, "Model $modelName not found")
        return false
    }

    return true
}
