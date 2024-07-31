package dev.onelenyk.crudfather.app.routing.controllers

import com.mongodb.client.model.Updates
import dev.onelenyk.crudfather.domain.repository.ModelSchemeRepository
import dev.onelenyk.crudfather.domain.scheme.DynamicModelManager.generateModelDefinition
import dev.onelenyk.crudfather.domain.models.DynamicModelScheme
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.*

class ModelSchemeController(private val repository: ModelSchemeRepository) {

    suspend fun create(call: ApplicationCall) {
        try {
            val modelName = call.parameters["name"].orEmpty()

            if (modelName.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Model name is required")
                return
            }

            val json = call.receiveText()
            val modelDefinition = generateModelDefinition(modelName, json)
            val modelScheme = DynamicModelScheme(definition = modelDefinition)
            repository.create(modelScheme)
            call.respond(HttpStatusCode.Created, modelScheme)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid JSON: ${e.message}")
        }
    }

    suspend fun readAll(call: ApplicationCall) {
        try {
            val list = repository.readAll()
            call.respond(HttpStatusCode.OK, list)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Error fetching models: ${e.message}")
        }
    }

    suspend fun getById(call: ApplicationCall) {
        val id = call.parameters["id"]
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid model ID")
            return
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

    suspend fun update(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid model ID")
                return
            }

            val json = call.receiveText()

            val model = repository.getById(UUID.fromString(id))

            if (model == null) {
                call.respond(HttpStatusCode.NotFound, "Model not found")
                return
            }

            val modelDefinition = generateModelDefinition(model.definition.modelName, json)

            val updates =
                Updates.combine(
                    Updates.set(DynamicModelScheme::definition.name, modelDefinition),
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

    suspend fun delete(call: ApplicationCall) {
        try {

            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid model ID")
                return@delete
            }

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