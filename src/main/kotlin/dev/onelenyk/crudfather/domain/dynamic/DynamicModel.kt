package dev.onelenyk.crudfather.domain.dynamic

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.bson.Document

@Serializable
data class DynamicModel(
    val modelName: String,
    val data: JsonObject,
) {
    companion object {
        fun fromString(
            modelName: String,
            json: String,
        ): DynamicModel {
            val jsonData = Json.parseToJsonElement(json).jsonObject

            val dynamicModel =
                DynamicModel(
                    modelName = modelName,
                    data = jsonData,
                )

            return dynamicModel
        }

        fun fromDocument(
            modelName: String,
            document: Document,
        ): DynamicModel {
            val json = document.toJson()
            val jsonData = Json.parseToJsonElement(json).jsonObject

            val dynamicModel =
                DynamicModel(
                    modelName = modelName,
                    data = jsonData,
                )

            return dynamicModel
        }
    }

    fun toJsonInput(): JsonObject {
        return data.changeIdToMongoDbId()
    }

    fun toJsonOutput(): JsonObject {
        return data.changeMongoDbIdToId()
    }

    private fun JsonObject.changeIdToMongoDbId(): JsonObject {
        val mutableMap = toMutableMap()
        val id = mutableMap.remove("id") ?: return this // Remove "id" and get its value
        mutableMap["_id"] = id // Add "_id" with the same value
        return JsonObject(mutableMap)
    }

    private fun JsonObject.changeMongoDbIdToId(): JsonObject {
        val mutableMap = toMutableMap()
        val objectId = mutableMap.remove("_id") ?: return this // Remove "_id" and get its value
        mutableMap["id"] = objectId // Add "id" with the same value
        return JsonObject(mutableMap)
    }
}
