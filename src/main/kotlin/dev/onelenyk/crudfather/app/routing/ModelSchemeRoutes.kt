package dev.onelenyk.crudfather.app.routing

import com.mongodb.client.model.Updates
import dev.onelenyk.crudfather.data.scheme.DynamicModelManager.generateModelDefinition
import dev.onelenyk.crudfather.data.scheme.ModelScheme
import dev.onelenyk.crudfather.repository.ModelSchemeRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

class ModelSchemeRoutes(private val repository: ModelSchemeRepository) {
    fun registerRoutes(routing: Routing) {
        routing.route("/models") {
            post {
                val modelName = call.parameters["name"].orEmpty()

                if (modelName.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Model name is required")
                    return@post
                }

                try {
                    val json = call.receiveText()
                    val modelDefinition = generateModelDefinition(modelName, json)
                    val modelScheme = ModelScheme(definition = modelDefinition)
                    repository.create(modelScheme)
                    call.respond(HttpStatusCode.Created, modelScheme)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid JSON: ${e.message}")
                }
            }

            get {
                try {
                    val list = repository.readAll()
                    call.respond(HttpStatusCode.OK, list)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error fetching models: ${e.message}")
                }
            }

            get("/{id}") {
                val id = call.parameters["id"]
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid model ID")
                    return@get
                }

                try {
                    val model = repository.getById(UUID.fromString(id))
                    if (model != null) {
                        call.respond(HttpStatusCode.OK, model)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Model not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error fetching model: ${e.message}")
                }
            }

            put("/{id}") {
                val id = call.parameters["id"]
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid model ID")
                    return@put
                }

                try {
                    val json = call.receiveText()

                    val model = repository.getById(UUID.fromString(id))

                    if (model == null) {
                        call.respond(HttpStatusCode.NotFound, "Model not found")
                        return@put
                    }

                    val modelDefinition = generateModelDefinition(model.definition.modelName, json)

                    val updates =
                        Updates.combine(
                            Updates.set(ModelScheme::definition.name, modelDefinition),
                        )

                    val updatedModel = repository.update(UUID.fromString(id), updates)
                    if (updatedModel != null) {
                        call.respond(HttpStatusCode.OK, updatedModel)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Model not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error updating model: ${e.message}")
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"]
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid model ID")
                    return@delete
                }

                try {
                    val deleted = repository.delete(UUID.fromString(id))
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, "Model deleted")
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Model not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error deleting model: ${e.message}")
                }
            }
        }
    }
}
