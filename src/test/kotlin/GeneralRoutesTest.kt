import dev.onelenyk.crudfather.app.Server
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class GeneralRoutesTest : BaseTest() {
    @BeforeTest
    fun setup() {
        setupApiClients(port = 8080)
    }

    @Test
    fun testLiveRoute() = runBlocking {
        val response = generalApiClient.checkLive()
        TestCase.assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testRoutesRoute() = runBlocking {
        val response = generalApiClient.getRoutes()
        TestCase.assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testDocRoute() = runBlocking {
        val response = generalApiClient.getDoc()
        TestCase.assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun testRootRoute() = runBlocking {
        val response = generalApiClient.getRoot()
        TestCase.assertEquals(HttpStatusCode.OK, response.status)
    }
}