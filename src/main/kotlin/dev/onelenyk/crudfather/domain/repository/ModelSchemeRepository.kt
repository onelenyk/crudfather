package dev.onelenyk.crudfather.domain.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.kotlin.client.coroutine.MongoCollection
import dev.onelenyk.crudfather.domain.scheme.ModelScheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson
import java.util.*

class ModelSchemeRepository(private val collection: MongoCollection<ModelScheme>) {
    suspend fun create(model: ModelScheme): ModelScheme {
        val insertOne = collection.insertOne(model)
        val filter = Filters.eq("_id", insertOne.insertedId)
        return collection.find(filter).first()
    }

    suspend fun getById(id: UUID): ModelScheme? {
        val filter = Filters.eq("_id", id)
        return collection.find(filter).firstOrNull()
    }

    suspend fun delete(id: UUID): Boolean {
        val filter = Filters.eq("_id", id)
        val result = collection.deleteOne(filter)
        return result.deletedCount > 0
    }

    suspend fun modelExistsByName(modelName: String): Boolean {
        val filter = Filters.eq("definition.modelName", modelName)
        return collection.find(filter).firstOrNull() != null
    }

    suspend fun getModelSchemeByDefinitionName(modelName: String): ModelScheme? {
        val filter = Filters.eq("definition.modelName", modelName)
        return collection.find(filter).firstOrNull()
    }

    suspend fun readAll(): List<ModelScheme> {
        return collection.find().toList()
    }

    suspend fun update(
        id: UUID,
        document: Bson,
    ): ModelScheme? {
        val filter = Filters.eq("_id", id)
        collection.updateOne(filter, document, UpdateOptions().upsert(true))
        return getById(id)
    }
}
