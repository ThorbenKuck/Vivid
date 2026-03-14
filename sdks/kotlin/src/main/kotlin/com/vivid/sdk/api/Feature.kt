package com.vivid.sdk.api

import java.time.Instant
import kotlin.reflect.KClass

data class Feature(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val flags: Map<String, Boolean>,
    val metadata: Map<String, MetadataValue>,
    val timestamp: Instant,
) {
    fun <T: MetadataValue> checkedMetadataValue(name: String, type: KClass<T>): T? {
        val unchecked = metadata[name] ?: return null
        if (type.isInstance(unchecked)) {
            return unchecked as T
        } else {
            throw ClassCastException("Expected metadata $name to be of type ${type.qualifiedName}, but was of type ${unchecked::class.qualifiedName}")
        }
    }
}

inline fun <reified T: MetadataValue> Feature.checkedMetadataValue(name: String): T? {
    return checkedMetadataValue(name, T::class)
}
