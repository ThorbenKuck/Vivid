package com.vivid.sdk.api.metadata

import tools.jackson.databind.JsonNode

import com.vivid.sdk.api.MetadataValue
import tools.jackson.databind.ObjectMapper
import kotlin.reflect.KClass

/**
 * Metadata value containing a string that is a JSON.
 *
 * @property content the JSON string
 */
data class JsonMetadataValue(
    val content: JsonNode
) : MetadataValue {
    fun <T: Any>convertTo(objectMapper: ObjectMapper, clazz: KClass<T>): T {
        return objectMapper.treeToValue(content, clazz.java)
    }
}

inline fun <reified T: Any>JsonMetadataValue.convertTo(objectMapper: ObjectMapper): T {
    return convertTo(objectMapper, T::class)
}

fun <T: Any>ObjectMapper.readAs(metadata: JsonMetadataValue, type: KClass<T>): T {
    return metadata.convertTo(this, type)
}

inline fun <reified T: Any>ObjectMapper.readAs(metadata: JsonMetadataValue): T {
    return readAs(metadata, T::class)
}
