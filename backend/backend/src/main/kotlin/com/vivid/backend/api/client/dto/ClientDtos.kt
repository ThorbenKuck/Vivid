package com.vivid.backend.api.client.dto

import com.vivid.backend.domain.entity.FeatureEnvironment
import java.time.Instant

data class ClientFeatureDto(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val flags: Map<String, Boolean>,
    val metadata: Map<String, com.vivid.backend.domain.entity.MetadataValue>,
    val timestamp: Instant = Instant.now(),
)

fun FeatureEnvironment.toClientDto() = ClientFeatureDto(
    id = feature.id.toString(),
    name = feature.name,
    enabled = enabled,
    flags = flags,
    metadata = metadata
)
