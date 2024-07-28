import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.*
import kotlin.test.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelRoutesTest : BaseTest() {
    val modelName = "testin100"

    fun createDocumentJson() =
        """ 
            {
            "id": "${UUID.randomUUID()}",
            "name": "Name",
            "age": 25,
            "email": "jane.doe@example.com",
            "isActive": true
        }
        """.trimIndent()

    private val sampleModelJson = createDocumentJson()

    @BeforeTest
    fun setup() {
        setupApiClients(port = 8080)
    }

    @Test
    fun testCreateModel() =
        runBlocking {
            val response = modelRoutesApiClient.createModel(modelName, sampleModelJson)
            assertEquals(HttpStatusCode.Created, response.status)
        }

    @Test
    fun testGetAllModels() =
        runBlocking {
            val response = modelRoutesApiClient.getModels()
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun testGetModelById() =
        runBlocking {
            val createResponse = modelRoutesApiClient.createModel(modelName, sampleModelJson)
            val body = createResponse.bodyAsText()
            val id = Json.parseToJsonElement(body).jsonObject["id"]?.jsonPrimitive?.content ?: return@runBlocking

            val response = modelRoutesApiClient.getModelById(id)
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun testUpdateModel() =
        runBlocking {
            val createResponse = modelRoutesApiClient.createModel(modelName, sampleModelJson)
            val body = createResponse.bodyAsText()
            val id = Json.parseToJsonElement(body).jsonObject["id"]?.jsonPrimitive?.content ?: return@runBlocking

            val response = modelRoutesApiClient.updateModel(id, """{"name":"Updated Model"}""")
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun testDeleteModel() =
        runBlocking {
            val createResponse = modelRoutesApiClient.createModel(modelName, sampleModelJson)
            val body = createResponse.bodyAsText()
            val id = Json.parseToJsonElement(body).jsonObject["id"]?.jsonPrimitive?.content ?: return@runBlocking

            val response = modelRoutesApiClient.deleteModel(id)
            assertEquals(HttpStatusCode.OK, response.status)
        }
}
