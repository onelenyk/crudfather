import dev.onelenyk.crudfather.app.Server
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.testing.*
import org.junit.AfterClass
import org.junit.BeforeClass

fun ApplicationTestBuilder.setupTestApplication(server: Server) {
    environment {
        config =
            MapApplicationConfig(
                "ktor.deployment.port" to 8080.toString(),
            )
    }
    application {
        server.module(this)
    }
}

class GeneralRoutesApiClient(private val client: HttpClient, private val baseUrl: String) {
    suspend fun checkLive(): HttpResponse {
        return client.get("$baseUrl/live")
    }

    suspend fun getRoutes(): HttpResponse {
        return client.get("$baseUrl/routes")
    }

    suspend fun getDoc(): HttpResponse {
        return client.get("$baseUrl/doc")
    }

    suspend fun getRoot(): HttpResponse {
        return client.get(baseUrl)
    }
}

open class BaseTest {
    protected lateinit var generalApiClient: GeneralRoutesApiClient
    protected lateinit var modelRoutesApiClient: ModelRoutesApiClient
    protected lateinit var dynamicRoutesApiClient: DynamicRoutesApiClient

    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            // Global setup if necessary
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            // Global teardown if necessary
        }
    }

    protected fun setupApiClients(port: Int = 8080) {
        val client =
            HttpClient(CIO) {
                followRedirects = false
                install(Logging)
            }

        val baseUrl = "http://localhost:$port"
        generalApiClient = GeneralRoutesApiClient(client, baseUrl)
        modelRoutesApiClient = ModelRoutesApiClient(client, baseUrl)
        dynamicRoutesApiClient = DynamicRoutesApiClient(client, baseUrl)
    }
}

class ModelRoutesApiClient(private val client: HttpClient, private val baseUrl: String) {
    suspend fun createModel(
        modelName: String,
        modelJson: String,
    ): HttpResponse {
        return client.post("$baseUrl/models?name=$modelName") {
            contentType(ContentType.Application.Json)
            setBody(modelJson)
        }
    }

    suspend fun getModels(): HttpResponse {
        return client.get("$baseUrl/models")
    }

    suspend fun getModelById(id: String): HttpResponse {
        return client.get("$baseUrl/models/$id")
    }

    suspend fun updateModel(
        id: String,
        updates: String,
    ): HttpResponse {
        return client.put("$baseUrl/models/$id") {
            contentType(ContentType.Application.Json)
            setBody(updates)
        }
    }

    suspend fun deleteModel(id: String): HttpResponse {
        return client.delete("$baseUrl/models/$id")
    }
}

class DynamicRoutesApiClient(private val client: HttpClient, private val baseUrl: String) {
    val dynamic = "$baseUrl/dynamic"

    suspend fun createDynamicDocument(
        modelName: String,
        documentJson: String,
    ): HttpResponse {
        return client.post("$dynamic/$modelName") {
            contentType(ContentType.Application.Json)
            setBody(documentJson)
        }
    }

    suspend fun getAllDynamicDocuments(modelName: String): HttpResponse {
        return client.get("$dynamic/$modelName")
    }

    suspend fun getDynamicDocumentById(
        modelName: String,
        id: String,
    ): HttpResponse {
        return client.get("$dynamic/$modelName/$id")
    }

    suspend fun updateDynamicDocument(
        modelName: String,
        id: String,
        updates: String,
    ): HttpResponse {
        return client.put("$dynamic/$modelName/$id") {
            contentType(ContentType.Application.Json)
            setBody(updates)
        }
    }

    suspend fun deleteDynamicDocument(
        modelName: String,
        id: String,
    ): HttpResponse {
        return client.delete("$dynamic/$modelName/$id")
    }
}
