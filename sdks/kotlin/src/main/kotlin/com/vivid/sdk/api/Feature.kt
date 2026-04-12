package com.vivid.sdk.api

import java.time.Instant
import kotlin.reflect.KClass

/**
 * Domain entity representing the state of a feature flag.
 *
 * @property id the unique identifier of the feature
 * @property name the human-readable name of the feature
 * @property enabled whether the main feature flag is enabled
 * @property flags a map of sub-flags and their enabled states
 * @property metadata a map of metadata keys and their values
 * @property timestamp the timestamp of when this feature state was captured
 */
data class Feature(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val flags: Map<String, Boolean>,
    val metadata: Map<String, MetadataValue>,
    val timestamp: Instant,
) {
    /**
     * Get a metadata value for the feature, checking its type.
     *
     * @param name the name of the metadata value
     * @param type the expected type of the metadata value
     * @return the metadata value if it exists and has the correct type, or null if it does not exist
     * @throws ClassCastException if the metadata value exists but has a different type
     */
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
