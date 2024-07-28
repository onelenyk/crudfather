package dev.onelenyk.crudfather.domain.scheme

import dev.onelenyk.crudfather.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

@Serializable
data class ModelScheme(
    @Serializable(with = UUIDSerializer::class) @BsonId val id: UUID = UUID.randomUUID(),
    val definition: ModelDefinition,
)
