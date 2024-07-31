package dev.onelenyk.crudfather.domain.scheme

import dev.onelenyk.crudfather.domain.models.DynamicModel
import dev.onelenyk.crudfather.domain.models.DynamicModelDefinition
import dev.onelenyk.crudfather.domain.models.DynamicModelDefinition.FieldDefinition
import dev.onelenyk.crudfather.domain.models.DynamicModelDefinition.FieldDefinition.FieldType
import kotlinx.serialization.json.*

object DynamicModelManager {

    data class ValidationResult(val isValid: Boolean, val log: List<String>)

    fun generateModelDefinition(
        modelName: String,
        json: String,
    ): DynamicModelDefinition {
        val jsonObject = Json.parseToJsonElement(json).jsonObject
        val fields = parseJsonObject(jsonObject)
        return DynamicModelDefinition(modelName, fields)
    }

    fun validateDynamicModel(
        modelDefinition: DynamicModelDefinition,
        dynamicModel: DynamicModel,
    ): ValidationResult {
        val log = mutableListOf<String>()
        val isValid = validateJsonModel(modelDefinition, dynamicModel.toJsonOutput(), log)
        return ValidationResult(isValid, log)
    }

    private fun parseJsonObject(jsonObject: JsonObject): List<FieldDefinition> {
        val fields = mutableListOf<FieldDefinition>()
        for ((key, value) in jsonObject) {
            val fieldType = determineFieldType(value)
            val fieldDefinition =
                when (fieldType) {
                    FieldType.OBJECT ->
                        FieldDefinition(
                            name = key,
                            type = fieldType,
                            nestedFields = parseJsonObject(value.jsonObject),
                        )

                    FieldType.ARRAY ->
                        FieldDefinition(
                            name = key,
                            type = fieldType,
                            elementType = determineArrayElementType(value.jsonArray),
                        )

                    else -> FieldDefinition(name = key, type = fieldType)
                }
            fields.add(fieldDefinition)
        }
        return fields
    }

    private fun validateJsonModel(
        modelDefinition: DynamicModelDefinition,
        jsonData: JsonObject,
        log: MutableList<String>,
    ): Boolean {
        var isValid = true
        for (field in modelDefinition.fields) {
            val jsonElement = jsonData[field.name]
            if (jsonElement == null) {
                log.add("Field '${field.name}' is missing")
                continue
            }

            when (field.type) {
                FieldType.STRING -> {
                    if (!jsonElement.jsonPrimitive.isString) {
                        log.add("Field '${field.name}' is not a valid string")
                        isValid = false
                    }
                }

                FieldType.INTEGER -> {
                    if (jsonElement.jsonPrimitive.intOrNull == null) {
                        log.add("Field '${field.name}' is not a valid integer")
                        isValid = false
                    }
                }

                FieldType.BOOLEAN -> {
                    if (jsonElement.jsonPrimitive.booleanOrNull == null) {
                        log.add("Field '${field.name}' is not a valid boolean")
                        isValid = false
                    }
                }

                FieldType.OBJECT -> {
                    if (!validateJsonModel(
                            DynamicModelDefinition(field.name, field.nestedFields ?: emptyList()),
                            jsonElement.jsonObject,
                            log
                        )
                    ) {
                        log.add("Field '${field.name}' is not a valid object")
                        isValid = false
                    }
                }

                FieldType.ARRAY -> {
                    if (!validateJsonArray(field, jsonElement.jsonArray, log)) {
                        log.add("Field '${field.name}' is not a valid array")
                        isValid = false
                    }
                }
            }
        }
        return isValid
    }

    private fun validateJsonArray(
        field: FieldDefinition,
        jsonArray: JsonArray,
        log: MutableList<String>,
    ): Boolean {
        val elementType = field.elementType ?: return false
        for (element in jsonArray) {
            when (elementType) {
                FieldType.STRING ->
                    if (!element.jsonPrimitive.isString) {
                        log.add("Array element in field '${field.name}' is not a valid string")
                        return false
                    }

                FieldType.INTEGER ->
                    if (element.jsonPrimitive.intOrNull == null) {
                        log.add("Array element in field '${field.name}' is not a valid integer")
                        return false
                    }

                FieldType.BOOLEAN ->
                    if (element.jsonPrimitive.booleanOrNull == null) {
                        log.add("Array element in field '${field.name}' is not a valid boolean")
                        return false
                    }

                FieldType.OBJECT ->
                    if (!validateJsonModel(
                            DynamicModelDefinition(field.name, field.nestedFields ?: emptyList()),
                            element.jsonObject,
                            log
                        )
                    ) {
                        log.add("Array element in field '${field.name}' is not a valid object")
                        return false
                    }

                FieldType.ARRAY -> {
                    log.add("Nested arrays are not supported for field '${field.name}'")
                    return false
                }
            }
        }
        return true
    }

    private fun determineFieldType(jsonElement: JsonElement): FieldType {
        return when (jsonElement) {
            is JsonPrimitive ->
                when {
                    jsonElement.isString -> FieldType.STRING
                    jsonElement.booleanOrNull != null -> FieldType.BOOLEAN
                    jsonElement.intOrNull != null -> FieldType.INTEGER
                    else -> throw IllegalArgumentException("Unknown primitive type")
                }

            is JsonObject -> FieldType.OBJECT
            is JsonArray -> FieldType.ARRAY
            else -> throw IllegalArgumentException("Unknown field type")
        }
    }

    private fun determineArrayElementType(jsonArray: JsonArray): FieldType {
        if (jsonArray.isEmpty()) {
            throw IllegalArgumentException("Array cannot be empty")
        }
        val firstElement = jsonArray.first()
        return determineFieldType(firstElement)
    }
}
