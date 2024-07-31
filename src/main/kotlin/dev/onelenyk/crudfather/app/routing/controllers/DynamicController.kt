package dev.onelenyk.crudfather.app.routing.controllers

import dev.onelenyk.crudfather.domain.models.DynamicModel
import dev.onelenyk.crudfather.domain.repository.DynamicRepository
import dev.onelenyk.crudfather.domain.repository.ModelSchemeRepository
import dev.onelenyk.crudfather.domain.scheme.DynamicModelManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json

class DynamicController (
    private val repository: ModelSchemeRepository, private val dynamicRepository: DynamicRepository
) {
    private val dynamicModelManager: DynamicModelManager = DynamicModelManager

    suspend fun create(call: ApplicationCall) {
        try {
            val modelName = call.parameters["model"].orEmpty()
            val json = call.receiveText()

            if (modelName.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Model name is required")
                return
            }

            if (!repository.modelExistsByName(modelName)) {
                call.respond(HttpStatusCode.NotFound, "Model is not exist")
                return
            }

            val modelScheme = repository.getModelSchemeByDefinitionName(modelName)
            if (modelScheme == null) {
                call.respond(HttpStatusCode.NotFound, "Model definition for $modelName not found")
                return
            }

            val dynamicModel = DynamicModel.fromString(modelName, json)
            val inputModel = dynamicModel.toJsonInput()

            val valid = dynamicModelManager.validateDynamicModel(modelScheme.definition, dynamicModel)
            if (!valid.isValid) {
                call.respond(HttpStatusCode.BadRequest, "Invalid JSON: does not match the model definition")
                return
            }

            val created = dynamicRepository.createDocument(modelName, inputModel)
            val result = DynamicModel.fromDocument(modelName, created).toJsonOutput()
            call.respond(HttpStatusCode.Created, result)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Error creating document: ${e.message}")
        }
    }

    suspend fun readAll(call: ApplicationCall) {
        try {
            val modelName = call.parameters["model"].orEmpty()

            if (!repository.modelExistsByName(modelName)) {
                call.respond(HttpStatusCode.NotFound, "Model is not exist")
                return
            }

            val output = dynamicRepository.getAllDocuments(modelName).map {
                DynamicModel.fromDocument(modelName, it).toJsonOutput()
            }.toString()
            val test = Json.parseToJsonElement(output)
            call.respond(HttpStatusCode.Found, test)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error fetching documents: ${e.message}")
        }
    }

    suspend fun getById(call: ApplicationCall) {
        val modelName = call.parameters["model"].orEmpty()
        val id = call.parameters["id"].orEmpty()

        if (!repository.modelExistsByName(modelName)) {
            call.respond(HttpStatusCode.NotFound, "Model is not exist")
            return
        }
        try {
            val document = dynamicRepository.getDocumentById(modelName, id)
            if (document != null) {
                val result = DynamicModel.fromDocument(modelName, document).toJsonOutput()
                call.respond(HttpStatusCode.Found, result)
            } else {
                call.respond(HttpStatusCode.NotFound, "Document not found")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error fetching document: ${e.message}")
        }
    }

    suspend fun update(call: ApplicationCall) {
        try {
            val modelName = call.parameters["model"].orEmpty()
            val id = call.parameters["id"].orEmpty()
            val json = call.receiveText()

            if (modelName.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Model name is required")
                return
            }

            if (id.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Model ID is required")
                return
            }

            if (!repository.modelExistsByName(modelName)) {
                call.respond(HttpStatusCode.NotFound, "Model is not exist")
                return
            }

            val modelScheme = repository.getModelSchemeByDefinitionName(modelName)
            if (modelScheme == null) {
                call.respond(HttpStatusCode.NotFound, "Model definition for $modelName not found")
                return
            }
            val dynamicModel = DynamicModel.fromString(modelName, json)
            val inputModel = dynamicModel.toJsonInput()

            val valid = dynamicModelManager.validateDynamicModel(modelScheme.definition, dynamicModel)
            if (!valid.isValid) {
                call.respond(HttpStatusCode.BadRequest, "Invalid JSON: does not match the model definition")
                return
            }

            val updatedDocument = dynamicRepository.updateDocument(modelName, id, inputModel)

            if (updatedDocument != null) {
                val result = DynamicModel.fromDocument(modelName, updatedDocument).toJsonOutput()
                call.respond(HttpStatusCode.OK, result)
            } else {
                call.respond(HttpStatusCode.NotFound, "Document not found")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Error updating document: ${e.message}")
        }
    }

    suspend fun delete(call: ApplicationCall) {
        val modelName = call.parameters["model"].orEmpty()
        val id = call.parameters["id"].orEmpty()
        if (!repository.modelExistsByName(modelName)) {
            call.respond(HttpStatusCode.NotFound, "Model is not exist")
            return
        }

        try {
            val deleted = dynamicRepository.deleteDocument(modelName, id)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "Document deleted")
            } else {
                call.respond(HttpStatusCode.NotFound, "Document not found")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error deleting document: ${e.message}")
        }
    }
}