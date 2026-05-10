package com.vivid.backend.clients.api.dto

import com.vivid.backend.domain.entity.BooleanMetadataValue
import com.vivid.backend.domain.entity.DoubleMetadataValue
import com.vivid.backend.domain.entity.JsonMetadataValue
import com.vivid.backend.domain.entity.LongMetadataValue
import com.vivid.backend.domain.entity.infrastructure.FeatureEntity
import com.vivid.backend.domain.entity.MetadataValue
import com.vivid.backend.domain.entity.StringListMetadataValue
import com.vivid.backend.domain.entity.StringMetadataValue
import com.vivid.clients.api.Feature
import com.vivid.clients.api.metadata.BooleanMetadataValue as ClientBooleanMetadataValue
import com.vivid.clients.api.metadata.LongMetadataValue as ClientLongMetadataValue
import com.vivid.clients.api.metadata.DoubleMetadataValue as ClientDoubleMetadataValue
import com.vivid.clients.api.metadata.StringMetadataValue as ClientStringMetadataValue
import com.vivid.clients.api.metadata.StringListMetadataValue as ClientStringListMetadataValue
import com.vivid.clients.api.metadata.JsonMetadataValue as ClientJsonMetadataValue
import com.vivid.clients.api.MetadataValue as ClientMetadataValue
import java.time.Instant
import java.util.*

fun FeatureEntity.toClientDto(environmentId: UUID?): Feature {
    val resolved = this.resolve(environmentId)
    return Feature(
        this.id.toString(),
        this.name,
        this.key,
        resolved.enabled,
        resolved.flags,
        resolved.metadata.mapValues { it.value.toClientDto() },
        Instant.now()
    )
}

fun <T: Any> MetadataValue<T>.toClientDto(): ClientMetadataValue<T> {
    return when(this) {
        is BooleanMetadataValue -> ClientBooleanMetadataValue(content)
        is LongMetadataValue -> ClientLongMetadataValue(content)
        is DoubleMetadataValue -> ClientDoubleMetadataValue(content)
        is StringMetadataValue -> ClientStringMetadataValue(content)
        is StringListMetadataValue -> ClientStringListMetadataValue(content)
        is JsonMetadataValue -> ClientJsonMetadataValue(content)
    } as ClientMetadataValue<T>
}
