package com.vivid.sdk.api

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.vivid.sdk.api.metadata.BooleanMetadataValue
import com.vivid.sdk.api.metadata.DoubleMetadataValue
import com.vivid.sdk.api.metadata.JsonMetadataValue
import com.vivid.sdk.api.metadata.LongMetadataValue
import com.vivid.sdk.api.metadata.StringListMetadataValue
import com.vivid.sdk.api.metadata.StringMetadataValue

/**
 * Base interface for all metadata values in a feature flag.
 *
 * Metadata can be used to store additional information about a feature flag, such as configuration parameters.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@type",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(value = BooleanMetadataValue::class, name = "Boolean"),
    JsonSubTypes.Type(value = LongMetadataValue::class, name = "Long"),
    JsonSubTypes.Type(value = DoubleMetadataValue::class, name = "Double"),
    JsonSubTypes.Type(value = StringMetadataValue::class, name = "String"),
    JsonSubTypes.Type(value = JsonMetadataValue::class, name = "Json"),
    JsonSubTypes.Type(value = StringListMetadataValue::class, name = "StringList")
)
interface MetadataValue

fun MetadataValue?.isTrue(): Boolean? {
    return when (this) {
        is BooleanMetadataValue -> this.content
        is StringMetadataValue -> this.content.toBooleanStrictOrNull()
        else -> null
    }
}

fun MetadataValue?.isFalse(): Boolean? {
    return when (this) {
        is BooleanMetadataValue -> this.content
        is StringMetadataValue -> this.content.toBooleanStrictOrNull()
        else -> null
    }
}

fun MetadataValue?.contains(value: String): Boolean {
    return when (this) {
        is StringMetadataValue -> this.content.contains(value)
        is StringListMetadataValue -> this.content.contains(value)
        else -> false
    }
}

fun MetadataValue.biggerThan(value: Double): Boolean {
    return when (this) {
        is DoubleMetadataValue -> this.content > value
        is LongMetadataValue -> this.content > value
        else -> false
    }
}

fun MetadataValue.biggerThan(value: Long): Boolean {
    return when (this) {
        is DoubleMetadataValue -> this.content > value
        is LongMetadataValue -> this.content > value
        else -> false
    }
}

fun MetadataValue.smallerThan(value: Double): Boolean {
    return when (this) {
        is DoubleMetadataValue -> this.content < value
        is LongMetadataValue -> this.content < value
        else -> false
    }
}

fun MetadataValue.smallerThan(value: Long): Boolean {
    return when (this) {
        is DoubleMetadataValue -> this.content < value
        is LongMetadataValue -> this.content < value
        else -> false
    }
}
