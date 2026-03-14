package com.vivid.backend.domain.entity

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = BooleanMetadataValue::class, name = "Boolean"),
    JsonSubTypes.Type(value = LongMetadataValue::class, name = "Long"),
    JsonSubTypes.Type(value = DoubleMetadataValue::class, name = "Double"),
    JsonSubTypes.Type(value = StringMetadataValue::class, name = "String"),
    JsonSubTypes.Type(value = JsonMetadataValue::class, name = "Json"),
    JsonSubTypes.Type(value = StringListMetadataValue::class, name = "StringList")
)
@Schema(
    description = "Polymorphic metadata value",
    discriminatorProperty = "@type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "Boolean", schema = BooleanMetadataValue::class),
        DiscriminatorMapping(value = "Long", schema = LongMetadataValue::class),
        DiscriminatorMapping(value = "Double", schema = DoubleMetadataValue::class),
        DiscriminatorMapping(value = "String", schema = StringMetadataValue::class),
        DiscriminatorMapping(value = "Json", schema = JsonMetadataValue::class),
        DiscriminatorMapping(value = "StringList", schema = StringListMetadataValue::class)
    ],
    oneOf = [
        BooleanMetadataValue::class,
        LongMetadataValue::class,
        DoubleMetadataValue::class,
        StringMetadataValue::class,
        JsonMetadataValue::class,
        StringListMetadataValue::class
    ]
)
sealed class MetadataValue
