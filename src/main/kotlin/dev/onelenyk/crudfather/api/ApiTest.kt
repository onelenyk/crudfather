package dev.onelenyk.crudfather.api

import com.typesafe.config.ConfigException.Null
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*

class TestApiClient(
    private val client: HttpClient,
    private val baseUrl: String,
) {

    val dynamic = "$baseUrl/dynamic"

    suspend fun createModel(
        modelName: String,
        modelJson: String,
    ): HttpResponse {

        val response =
            client.post("$baseUrl/models?name=$modelName") {
                contentType(ContentType.Application.Json)
                setBody(modelJson)
            }

        return response
    }

    suspend fun getModels(): HttpResponse {
        val response = client.get("$baseUrl/models")
        println("Get Models Response: ${response.bodyAsText()}")
        return response
    }

    suspend fun getModelById(id: String): HttpResponse {
        val response = client.get("$baseUrl/models/$id")
        println("Get Model By ID Response: ${response.bodyAsText()}")
        return response
    }

    suspend fun updateModel(
        id: String,
        updates: String,
    ): HttpResponse {
        val response =
            client.put("$baseUrl/models/$id") {
                contentType(ContentType.Application.Json)
                setBody(updates)
            }
        println("Update Model Response: ${response.bodyAsText()}")
        return response

    }

    suspend fun deleteModel(id: String): HttpResponse {
        val response = client.delete("$baseUrl/models/$id")
        println("Delete Model Response: ${response.bodyAsText()}")
        return response
    }

    suspend fun createDynamicDocument(
        modelName: String,
        documentJson: String,
    ): HttpResponse {
        val response =
            client.post("$dynamic/$modelName") {
                contentType(ContentType.Application.Json)
                setBody(documentJson)
            }
        return response
    }

    suspend fun getAllDynamicDocuments(modelName: String): HttpResponse {
        val response = client.get("$dynamic/$modelName")
        return response
    }

    suspend fun getDynamicDocumentById(
        modelName: String,
        id: String,
    ): HttpResponse {
        val response = client.get("$dynamic/$modelName/$id")
        return response
    }

    suspend fun updateDynamicDocument(
        modelName: String,
        id: String,
        updates: String,
    ): HttpResponse {
        val response =
            client.put("$dynamic/$modelName/$id") {
                contentType(ContentType.Application.Json)
                setBody(updates)
            }
        println("Update Dynamic Document Response: ${response.bodyAsText()}")
        return response
    }

    suspend fun deleteDynamicDocument(
        modelName: String,
        id: String,
    ): HttpResponse {
        val response = client.delete("$dynamic/$modelName/$id")
        println("Delete Dynamic Document Response: ${response.bodyAsText()}")
        return response
    }
}

suspend fun main() {
    val client =
        HttpClient(CIO) {
            followRedirects = false
        }
    val apiClient = TestApiClient(client, "http://localhost:8080")

    val documentJson = createDocumentJson()
    val documentJsonNext = createDocumentJsonNext()

    testModelSchemeRoutes(apiClient, documentJson, documentJsonNext)
    testDynamicRoutes(apiClient, documentJson, documentJsonNext)

    client.close()
}

fun createDocumentJson() =
    """
    {
        "id": "${UUID.randomUUID()}",
        "name": "Jane Doe",
        "age": 25,
        "email": "jane.doe@example.com",
        "isActive": true
    }
    """.trimIndent()

fun createDocumentJsonNext() =
    """
    {
        "name": "Jane Doe NEXT",
        "age": 25,
        "email": "jane.doe@example.com NEXT",
        "isActive": true
    }
    """.trimIndent()

suspend fun testModelSchemeRoutes(
    apiClient: TestApiClient,
    modelJson: String,
    documentJsonNext: String,

) {
    val response = apiClient.createModel("projects", modelJson)
    val body = response.bodyAsText()
    val objextId = Json.parseToJsonElement(body).jsonObject.getValue("id").jsonPrimitive.content

    println("Create Model Response: $body")

    apiClient.getModels()
    delay(3000L)
    apiClient.getModelById(objextId)
    apiClient.updateDynamicDocument("projects",objextId, documentJsonNext)
//    apiClient.deleteModel(objextId)
}

suspend fun testDynamicRoutes(
    apiClient: TestApiClient,
    documentJson: String,
    documentJsonNext: String,
) {
    val response = apiClient.createDynamicDocument("projects", documentJson)
    val body = response.bodyAsText()

    println("Create Dynamic Document Response: $body")

    if (!response.status.isSuccess()) return

    val objextId = Json.parseToJsonElement(body).jsonObject.getValue("id").jsonPrimitive.content

    apiClient.getDynamicDocumentById("projects", objextId)
    apiClient.updateDynamicDocument("projects", objextId, documentJsonNext)
    //  apiClient.deleteDynamicDocument("users", objextId)
}
