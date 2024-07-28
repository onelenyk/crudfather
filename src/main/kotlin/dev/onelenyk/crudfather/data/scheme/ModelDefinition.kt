package dev.onelenyk.crudfather.data.scheme

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
enum class FieldType {
    STRING,
    INTEGER,
    BOOLEAN,
    OBJECT,
    ARRAY,
}

@Serializable
data class FieldDefinition(
    val name: String,
    val type: FieldType,
    val required: Boolean = true,
    val nestedFields: List<FieldDefinition>? = null,
    val elementType: FieldType? = null,
)

@Serializable
data class ModelDefinition(
    val modelName: String,
    val fields: List<FieldDefinition>,
)
