package dev.onelenyk.crudfather.app.di

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dev.onelenyk.crudfather.app.routing.ServerRouting
import dev.onelenyk.crudfather.domain.scheme.ModelScheme
import dev.onelenyk.crudfather.data.db.MongoDBManager
import dev.onelenyk.crudfather.domain.repository.DynamicRepository
import dev.onelenyk.crudfather.domain.repository.ModelSchemeRepository
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.koin.dsl.module

val koinModule =
    module {
        single { dotenv() }
        single { modelSchemeRepository(get()) }
        single { database(get()) }
        single { DynamicRepository(get()) }
        single { ServerRouting(get(), get()) }
        single { provideCoroutineScope() }
        single { provideDbCredentials(get()) }
        single { MongoDBManager(get()) }
    }

@OptIn(DelicateCoroutinesApi::class)
private fun provideCoroutineScope(): CoroutineScope {
    return GlobalScope
}

fun modelSchemeRepository(mongoDBManager: MongoDBManager): ModelSchemeRepository {
    val collection = mongoDBManager.getCollection("models", ModelScheme::class.java)
    return ModelSchemeRepository(collection)
}

fun database(mongoDBManager: MongoDBManager): MongoDatabase {
    val database = mongoDBManager.getDatabase()
    return database
}

fun provideDbCredentials(dotenv: Dotenv): DbCredentials {
    val username = dotenv["DB_USERNAME"]
    val pass = dotenv["DB_PASSWORD"]
    val connection = dotenv["DB_CONNECTION"]
    return DbCredentials(username, pass, connection)
}

fun provideServerPort(dotenv: Dotenv): Int {
    val port = dotenv["PORT"].toIntOrNull()
    return port ?: 8080
}

data class DbCredentials(val username: String, val password: String, val connection: String)
