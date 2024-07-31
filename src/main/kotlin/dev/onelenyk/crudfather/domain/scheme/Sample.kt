package dev.onelenyk.crudfather.domain.scheme

import dev.onelenyk.crudfather.domain.scheme.DynamicModelManager.generateModelDefinition
import kotlinx.serialization.json.*
import kotlin.random.Random

object Sample {
    private fun generateSampleJson(modelDefinition: ModelDefinition): JsonObject {
        val jsonObject =
            buildJsonObject {
                modelDefinition.fields.forEach { field ->
                    put(field.name, generateSampleValue(field))
                }
            }
        return jsonObject
    }

    private fun generateSampleValue(field: FieldDefinition): JsonElement {
        return when (field.type) {
            FieldType.STRING -> JsonPrimitive(generateRandomString())
            FieldType.INTEGER -> JsonPrimitive(generateRandomInt())
            FieldType.BOOLEAN -> JsonPrimitive(generateRandomBoolean())
            FieldType.OBJECT -> generateSampleJson(ModelDefinition(field.name, field.nestedFields ?: emptyList()))
            FieldType.ARRAY ->
                buildJsonArray {
                    repeat(3) {
                        when (field.elementType) {
                            FieldType.STRING -> add(JsonPrimitive(generateRandomString()))
                            FieldType.INTEGER -> add(JsonPrimitive(generateRandomInt()))
                            FieldType.BOOLEAN -> add(JsonPrimitive(generateRandomBoolean()))
                            FieldType.OBJECT ->
                                add(
                                    generateSampleJson(
                                        DynamicModelDefinition(
                                            field.name,
                                            field.nestedFields ?: emptyList(),
                                        ),
                                    ),
                                )

                            else -> throw IllegalArgumentException("Unsupported array element type")
                        }
                    }
                }
        }
    }

    private fun generateRandomString(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        return (1..10)
            .map { chars.random() }
            .joinToString("")
    }

    private fun generateRandomInt(): Int {
        return Random.nextInt(0, 100)
    }

    private fun generateRandomBoolean(): Boolean {
        return Random.nextBoolean()
    }

    fun main() {
        val json = """
{
    "id":"null",
    "name":"nazar",
    "age":"0",
    "items":[
      {"value": "Open", "onclick": "OpenDoc()"},
        {"value": "Open", "onclick": "OpenDoc()"}
    ]
}
    """

        val modelName = "User"
        val modelDefinition = generateModelDefinition(modelName, json)

        println(modelDefinition)

        val sampleJson = generateSampleJson(modelDefinition)
        println(Json.encodeToString(JsonObject.serializer(), sampleJson))
    }
}
