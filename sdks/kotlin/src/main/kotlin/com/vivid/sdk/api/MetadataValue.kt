package com.vivid.sdk.api

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.vivid.sdk.api.metadata.BooleanMetadataValue
import com.vivid.sdk.api.metadata.DoubleMetadataValue
import com.vivid.sdk.api.metadata.JsonMetadataValue
import com.vivid.sdk.api.metadata.LongMetadataValue
import com.vivid.sdk.api.metadata.StringListMetadataValue
import com.vivid.sdk.api.metadata.StringMetadataValue

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
