package com.vivid.backend.api.client.dto

import com.vivid.backend.domain.entity.FeatureEntity
import com.vivid.backend.domain.entity.MetadataValue
import java.time.Instant
import java.util.*

data class ClientFeatureDto(
    val id: String,
    val name: String,
    val key: String,
    val enabled: Boolean,
    val flags: Map<String, Boolean>,
    val metadata: Map<String, MetadataValue>,
    val timestamp: Instant = Instant.now(),
)

fun FeatureEntity.toClientDto(environmentId: UUID?): ClientFeatureDto {
    val resolved = this.resolve(environmentId)
    return ClientFeatureDto(
        id = this.id.toString(),
        name = this.name,
        key = this.key,
        enabled = resolved.enabled,
        flags = resolved.flags,
        metadata = resolved.metadata
    )
}
