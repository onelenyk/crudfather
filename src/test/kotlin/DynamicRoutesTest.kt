import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.*
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicRoutesTest : BaseTest() {
    val tryId = Random.nextInt().absoluteValue.toString()

    val modelName = "testing" + tryId

    val modelId = UUID.randomUUID()

    fun createDocumentJson() =
        """
        {
            "id": "$modelId",
            "title": "test #$tryId",
            "isActive": true
        }
        """.trimIndent()

    fun createDocumentJsonNext() =
        """
        {
            "title": "test #$tryId updated",
            "isActive": true,
            "additionalInfo": "nothing"
        }
        """.trimIndent()

    private val sampleDocumentJson = createDocumentJson()
    private val sampleUpdatedDocumentJson = createDocumentJsonNext()

    @BeforeTest
    fun setup() {
        setupApiClients(port = 8080)
    }

    @Test
    fun testCreateDynamicRoutes() =
        runBlocking {
            val responseCreate = modelRoutesApiClient.createModel(modelName, sampleDocumentJson)
            assertEquals(HttpStatusCode.Created, responseCreate.status)

            // Create Dynamic Document
            var response = dynamicRoutesApiClient.createDynamicDocument(modelName, sampleDocumentJson)
            assertEquals(HttpStatusCode.Created, response.status)
        }

    @Test
    fun testReadDynamicRoutes() =
        runBlocking {
            val responseCreate = modelRoutesApiClient.createModel(modelName, sampleDocumentJson)
            assertEquals(HttpStatusCode.Created, responseCreate.status)

            // Create Dynamic Document
            var responseCreateDynamic = dynamicRoutesApiClient.createDynamicDocument(modelName, sampleDocumentJson)
            assertEquals(HttpStatusCode.Created, responseCreateDynamic.status)

            var responseGet = dynamicRoutesApiClient.getDynamicDocumentById(modelName, modelId.toString())
            assertEquals(HttpStatusCode.Found, responseGet.status)
        }

    @Test
    fun testUpdateDynamicRoutes() =
        runBlocking {
            val responseCreate = modelRoutesApiClient.createModel(modelName, sampleDocumentJson)
            assertEquals(HttpStatusCode.Created, responseCreate.status)

            // Create Dynamic Document
            var responseCreateDynamic = dynamicRoutesApiClient.createDynamicDocument(modelName, sampleDocumentJson)
            assertEquals(HttpStatusCode.Created, responseCreateDynamic.status)

            var responseGet = dynamicRoutesApiClient.getDynamicDocumentById(modelName, modelId.toString())
            assertEquals(HttpStatusCode.Found, responseGet.status)

            val responseUpdate = dynamicRoutesApiClient.updateDynamicDocument(modelName, modelId.toString(), sampleUpdatedDocumentJson)
            assertEquals(HttpStatusCode.OK, responseUpdate.status)
        }

    @Test
    fun testDeleteDynamicRoutes() =
        runBlocking {
            val responseCreate = modelRoutesApiClient.createModel(modelName, sampleDocumentJson)
            assertEquals(HttpStatusCode.Created, responseCreate.status)

            // Create Dynamic Document
            var responseCreateDynamic = dynamicRoutesApiClient.createDynamicDocument(modelName, sampleDocumentJson)
            assertEquals(HttpStatusCode.Created, responseCreateDynamic.status)

            var responseGet = dynamicRoutesApiClient.getDynamicDocumentById(modelName, modelId.toString())
            assertEquals(HttpStatusCode.Found, responseGet.status)

            val responseUpdate = dynamicRoutesApiClient.updateDynamicDocument(modelName, modelId.toString(), sampleUpdatedDocumentJson)
            assertEquals(HttpStatusCode.OK, responseUpdate.status)

            val responseDelete = dynamicRoutesApiClient.deleteDynamicDocument(modelName, modelId.toString())
            assertEquals(HttpStatusCode.OK, responseUpdate.status)
        }
}
