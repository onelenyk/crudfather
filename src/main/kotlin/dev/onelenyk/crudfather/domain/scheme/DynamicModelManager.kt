package dev.onelenyk.crudfather.domain.scheme

import dev.onelenyk.crudfather.domain.dynamic.DynamicModel
import kotlinx.serialization.json.*

object DynamicModelManager {
    fun generateModelDefinition(
        modelName: String,
        json: String,
    ): ModelDefinition {
        val jsonObject = Json.parseToJsonElement(json).jsonObject
        val fields = parseJsonObject(jsonObject)
        return ModelDefinition(modelName, fields)
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

    data class ValidationResult(val isValid: Boolean, val log: List<String>)

    fun validateDynamicModel(
        modelDefinition: ModelDefinition,
        dynamicModel: DynamicModel,
    ): ValidationResult {
        val log = mutableListOf<String>()
        val isValid = validateJsonModel(modelDefinition, dynamicModel.toJsonOutput(), log)
        return ValidationResult(isValid, log)
    }

    private fun validateJsonModel(
        modelDefinition: ModelDefinition,
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
                    if (!validateJsonModel(ModelDefinition(field.name, field.nestedFields ?: emptyList()), jsonElement.jsonObject, log)) {
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
                    if (!validateJsonModel(ModelDefinition(field.name, field.nestedFields ?: emptyList()), element.jsonObject, log)) {
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

    fun validateDynamicModel2(
        modelDefinition: ModelDefinition,
        dynamicModel: DynamicModel,
    ): Boolean {
        val valid = validateJsonModel(modelDefinition = modelDefinition, dynamicModel.toJsonOutput())
        return valid
    }

    private fun validateJsonModel(
        modelDefinition: ModelDefinition,
        jsonData: JsonObject,
    ): Boolean {
        for (field in modelDefinition.fields) {
            val jsonElement = jsonData[field.name] ?: return false

            when (field.type) {
                FieldType.STRING -> if (!jsonElement.jsonPrimitive.isString) return false
                FieldType.INTEGER -> if (jsonElement.jsonPrimitive.intOrNull == null) return false
                FieldType.BOOLEAN -> if (jsonElement.jsonPrimitive.booleanOrNull == null) return false
                FieldType.OBJECT ->
                    if (!validateJsonModel(
                            ModelDefinition(field.name, field.nestedFields ?: emptyList()),
                            jsonElement.jsonObject,
                        )
                    ) {
                        return false
                    }

                FieldType.ARRAY -> if (!validateJsonArray(field, jsonElement.jsonArray)) return false
            }
        }
        return true
    }

    private fun validateJsonArray(
        field: FieldDefinition,
        jsonArray: JsonArray,
    ): Boolean {
        val elementType = field.elementType ?: return false
        for (element in jsonArray) {
            when (elementType) {
                FieldType.STRING -> if (!element.jsonPrimitive.isString) return false
                FieldType.INTEGER -> if (element.jsonPrimitive.intOrNull == null) return false
                FieldType.BOOLEAN -> if (element.jsonPrimitive.booleanOrNull == null) return false
                FieldType.OBJECT ->
                    if (!validateJsonModel(
                            ModelDefinition(field.name, field.nestedFields ?: emptyList()),
                            element.jsonObject,
                        )
                    ) {
                        return false
                    }

                FieldType.ARRAY -> return false // Nested arrays are not supported
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
