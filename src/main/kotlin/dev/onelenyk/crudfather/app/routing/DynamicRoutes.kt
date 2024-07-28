package dev.onelenyk.crudfather.app.routing

import dev.onelenyk.crudfather.domain.dynamic.DynamicModel
import dev.onelenyk.crudfather.domain.scheme.DynamicModelManager.validateDynamicModel
import dev.onelenyk.crudfather.domain.repository.DynamicRepository
import dev.onelenyk.crudfather.domain.repository.ModelSchemeRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

class DynamicRoutes(private val repository: ModelSchemeRepository, private val dynamicRepository: DynamicRepository) {
    fun registerRoutes(routing: Routing) {
        routing.route("dynamic/{model}") {
            post {
                try {
                    val modelName = call.parameters["model"].orEmpty()
                    val json = call.receiveText()

                    val dynamicModel =
                        DynamicModel.fromString(
                            modelName = modelName,
                            json = json,
                        )

                    val inputModel = dynamicModel.toJsonInput()

                    if (!checkModelExists(repository, modelName)) {
                        call.respond(HttpStatusCode.NotFound, "Model is not exist")
                        return@post
                    }

                    val modelScheme = repository.getModelSchemeByDefinitionName(modelName)
                    if (modelScheme == null) {
                        call.respond(HttpStatusCode.NotFound, "Model definition for $modelName not found")
                        return@post
                    }

                    val valid = validateDynamicModel(modelScheme.definition, dynamicModel)
                    val isValid = valid.isValid
                    if (!isValid) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid JSON: does not match the model definition")
                        return@post
                    }

                    val created = dynamicRepository.createDocument(modelName, inputModel)
                    val result =
                        DynamicModel.fromDocument(modelName, created)
                            .toJsonOutput()
                    call.respond(HttpStatusCode.Created, result)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Error creating document: ${e.message}")
                }
            }

            get {
                try {
                    val modelName = call.parameters["model"].orEmpty()

                    if (!checkModelExists(repository, modelName)) {
                        call.respond(HttpStatusCode.NotFound, "Model is not exist")
                        return@get
                    }

                    val output =
                        dynamicRepository.getAllDocuments(modelName)
                            .map {
                                DynamicModel.fromDocument(modelName, it)
                            }.map {
                                it.toJsonOutput()
                            }.toString()
                    val test = Json.parseToJsonElement(output)
                    call.respond(HttpStatusCode.Found, test)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching documents: ${e.message}")
                }
            }

            get("/{id}") {
                val modelName = call.parameters["model"].orEmpty()
                val id = call.parameters["id"].orEmpty()

                if (!checkModelExists(repository, modelName)) {
                    call.respond(HttpStatusCode.NotFound, "Model is not exist")
                    return@get
                }
                try {
                    val document = dynamicRepository.getDocumentById(modelName, id)
                    if (document != null) {
                        val result =
                            DynamicModel.fromDocument(modelName, document)
                                .toJsonOutput()
                        call.respond(HttpStatusCode.Found, result)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Document not found")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error fetching document: ${e.message}")
                }
            }

            put("/{id}") {
                try {
                    val modelName = call.parameters["model"].orEmpty()
                    val id = call.parameters["id"].orEmpty()

                    val json = call.receiveText()

                    val dynamicModel =
                        DynamicModel.fromString(
                            modelName = modelName,
                            json = json,
                        )

                    val inputModel = dynamicModel.toJsonInput()

                    if (!checkModelExists(repository, modelName)) {
                        call.respond(HttpStatusCode.NotFound, "Model is not exist")
                        return@put
                    }

                    val modelScheme = repository.getModelSchemeByDefinitionName(modelName)
                    if (modelScheme == null) {
                        call.respond(HttpStatusCode.NotFound, "Model definition for $modelName not found")
                        return@put
                    }

                    val valid = validateDynamicModel(modelScheme.definition, dynamicModel)
                    val isValid = valid.isValid
                    if (!isValid) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid JSON: does not match the model definition")
                        return@put
                    }

                    val updatedDocument = dynamicRepository.updateDocument(modelName, id, inputModel)

                    if (updatedDocument != null) {
                        val result =
                            DynamicModel.fromDocument(modelName, updatedDocument)
                                .toJsonOutput()
                        call.respond(HttpStatusCode.OK, result)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Document not found")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.BadRequest, "Error updating document: ${e.message}")
                }
            }

            delete("/{id}") {
                val modelName = call.parameters["model"].orEmpty()
                val id = call.parameters["id"].orEmpty()
                if (!checkModelExists(repository, modelName)) return@delete

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
    }
}
