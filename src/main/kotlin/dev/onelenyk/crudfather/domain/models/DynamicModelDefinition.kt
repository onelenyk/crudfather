package dev.onelenyk.crudfather.domain.models

import kotlinx.serialization.Serializable


@Serializable
data class DynamicModelDefinition(
    val modelName: String,
    val fields: List<FieldDefinition>,
){
    @Serializable
    data class FieldDefinition(
        val name: String,
        val type: FieldType,
        val required: Boolean = true,
        val nestedFields: List<FieldDefinition>? = null,
        val elementType: FieldType? = null,
    ){
        @Serializable
        enum class FieldType {
            STRING,
            INTEGER,
            BOOLEAN,
            OBJECT,
            ARRAY,
        }
    }
}