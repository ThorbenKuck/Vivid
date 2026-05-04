package com.vivid.backend.api.web.dto

import com.vivid.backend.domain.entity.*
import com.vivid.backend.domain.entity.infrastructure.FeatureEntity
import java.util.*

// FeatureEntity DTO represents a feature with environment-specific state (enabled/flags/metadata)
// for a given environment context.

data class FeatureDto(
    val id: UUID?,
    val runningNumber: Long,
    val name: String,
    val key: String,
    val description: String?,
    val enabled: Boolean,
    val flags: Map<String, Boolean>,
    val metadata: Map<String, MetadataValue>,
    val overrides: List<EnvironmentOverrideDto>,
    val tags: List<String>,
    val outgoingLinks: List<FeatureLinkDto>,
    val notes: List<NoteDto>
)

data class NoteDto(
    val id: UUID,
    val content: String,
    val authorName: String,
    val timestamp: java.time.Instant
)

fun Note.toDto(): NoteDto = NoteDto(
    id = id,
    content = content,
    authorName = author.username,
    timestamp = timestamp
)

data class EnvironmentOverrideDto(
    val environmentId: UUID,
    val environmentName: String,
    val enabled: Boolean?,
    val flags: Map<String, Boolean>,
    val metadata: Map<String, MetadataValue>,
    val strategy: OverrideStrategy
)

fun EnvironmentOverrideEntity.toDto(): EnvironmentOverrideDto = EnvironmentOverrideDto(
    environmentId = environment.id,
    environmentName = environment.name,
    enabled = enabled,
    flags = flags,
    metadata = metadata,
    strategy = strategy
)

fun FeatureEntity.toDto(allEnvironments: List<EnvironmentEntity>): FeatureDto {
    val overrideDtos = allEnvironments.map { env ->
        val override = environmentOverrides.find { it.environment.id == env.id }
        EnvironmentOverrideDto(
            environmentId = env.id,
            environmentName = env.name,
            enabled = override?.enabled,
            flags = override?.flags ?: emptyMap(),
            metadata = override?.metadata ?: emptyMap(),
            strategy = override?.strategy ?: OverrideStrategy.EXTEND
        )
    }
    return FeatureDto(
        id = id,
        runningNumber = runningNumber,
        name = name,
        key = key,
        description = description,
        enabled = enabled,
        flags = flags,
        metadata = metadata ?: emptyMap(),
        overrides = overrideDtos,
        tags = tags.toList(),
        outgoingLinks = outgoingLinks.map { it.toDto() },
        notes = notes.map { it.toDto() }
    )
}

data class FeatureLinkDto(
    val id: UUID?,
    val sourceFeatureId: UUID?,
    val targetFeatureId: UUID,
    val targetFeatureName: String? = null,
    val type: String?
)

data class FeatureCreateRequest(
    val name: String,
    val description: String?,
    val tags: List<String> = emptyList(),
    val enabled: Boolean = false,
    val flags: Map<String, Boolean> = emptyMap(),
    val metadata: Map<String, MetadataValue> = emptyMap()
)

data class FeatureUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val enabled: Boolean? = null,
    val flags: Map<String, Boolean>? = null,
    val metadata: Map<String, MetadataValue>? = null,
    val overrides: List<EnvironmentOverrideUpdateRequest>? = null
)

data class EnvironmentOverrideUpdateRequest(
    val environmentId: UUID,
    val enabled: Boolean? = null,
    val flags: Map<String, Boolean>? = null,
    val metadata: Map<String, MetadataValue>? = null,
    val strategy: OverrideStrategy? = null
)

data class NoteCreateRequest(
    val content: String
)

data class FeatureLinkCreateRequest(
    val targetFeatureId: UUID,
    val type: String?
)

// Mapping Extension Functions

fun FeatureLink.toDto(): FeatureLinkDto = FeatureLinkDto(
    id = this.id,
    sourceFeatureId = this.sourceFeature.id,
    targetFeatureId = this.targetFeature.id,
    targetFeatureName = this.targetFeature.name,
    type = this.type
)

// Environment DTOs

data class EnvironmentDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val key: String,
    val weight: Int?
)

fun EnvironmentEntity.toDto() = EnvironmentDto(
    id = this.id,
    name = this.name,
    key = this.key,
    description = this.description,
    weight = this.weight,
)

data class EnvironmentCreateRequest(
    val name: String,
    val description: String?
)

data class ClientRegistryDto(
    val id: UUID,
    val clientToken: String?,
    val clientName: String,
    val environmentId: UUID,
    val environmentName: String,
    val lastSeen: java.time.Instant?,
    val technologies: Set<String>,
    val clientVersion: String?,
    val isOnline: Boolean
)

fun VividClientEntity.toDto(onlineThreshold: java.time.Duration): ClientRegistryDto = ClientRegistryDto(
    id = id,
    clientToken = clientToken,
    clientName = clientName,
    environmentId = environment.id,
    environmentName = environment.name,
    lastSeen = lastSeen,
    technologies = technologies,
    clientVersion = clientVersion,
    isOnline = lastSeen?.isAfter(java.time.Instant.now().minus(onlineThreshold)) ?: false
)

