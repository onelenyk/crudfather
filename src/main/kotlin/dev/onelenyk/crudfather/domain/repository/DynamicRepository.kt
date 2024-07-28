package dev.onelenyk.crudfather.domain.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.JsonObject
import org.bson.Document

class DynamicRepository(private val database: MongoDatabase) {
    private fun getCollection(collectionName: String): MongoCollection<Document> {
        return database.getCollection(collectionName)
    }

    suspend fun createDocument(
        collectionName: String,
        json: JsonObject,
    ): Document {
        val collection = getCollection(collectionName)
        val document = Document.parse(json.toString())
        collection.insertOne(document)
        return document
    }

    suspend fun getAllDocuments(collectionName: String): List<Document> {
        val collection = getCollection(collectionName)
        return collection.find().take(5).toList()
    }

    suspend fun getDocumentById(
        collectionName: String,
        id: String,
    ): Document? {
        val collection = getCollection(collectionName)
        return collection.find(Document("_id", id)).firstOrNull()
    }

    suspend fun updateDocument(
        collectionName: String,
        id: String,
        json: JsonObject,
    ): Document? {
        val collection = getCollection(collectionName)
        val options =
            FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER)

        // Parse the JSON string into a Document
        val updateDoc = Document("\$set", Document.parse(json.toString()))

        // Perform the update operation
        collection.findOneAndUpdate(
            filter = Filters.eq("_id", id),
            update = updateDoc,
            options = options,
        )

        return getDocumentById(collectionName, id)
    }

    suspend fun deleteDocument(
        collectionName: String,
        id: String,
    ): Boolean {
        val collection = getCollection(collectionName)
        val result = collection.deleteOne(Document("_id", id))
        return result.deletedCount > 0
    }
}
