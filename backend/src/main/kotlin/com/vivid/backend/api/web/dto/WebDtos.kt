package com.vivid.backend.api.web.dto

import com.vivid.backend.domain.entity.*
import com.vivid.backend.domain.entity.VividClientPresenceEntity
import com.vivid.backend.domain.entity.infrastructure.FeatureEntity
import com.vivid.backend.domain.entity.infrastructure.FeatureUsageEntity
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
    val metadata: Map<String, MetadataValue<Any>>,
    val overrides: List<EnvironmentOverrideDto>,
    val tags: List<String>,
    val outgoingLinks: List<FeatureLinkDto>,
    val notes: List<NoteDto>,
    val usage: List<FeatureUsageDto> = emptyList()
)

data class FeatureUsageDto(
    val clientName: String,
    val featureKey: String,
    val featureName: String,
    val environmentId: UUID,
    val environmentName: String,
    val lastSeen: java.time.Instant
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
    authorName = author.name ?: author.username,
    timestamp = timestamp
)

data class EnvironmentOverrideDto(
    val environmentId: UUID,
    val environmentName: String,
    val enabled: Boolean?,
    val flags: Map<String, Boolean>,
    val metadata: Map<String, MetadataValue<Any>>,
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

fun FeatureEntity.toDto(
    allEnvironments: List<EnvironmentEntity>,
    usage: List<FeatureUsageEntity> = emptyList(),
): FeatureDto {
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
        notes = notes.map { it.toDto() },
        usage = usage.map { it.toDto() }
    )
}

fun FeatureUsageEntity.toDto(): FeatureUsageDto = FeatureUsageDto(
    clientName = client.clientName,
    featureKey = feature.key,
    featureName = feature.name,
    environmentId = environment.id,
    environmentName = environment.name,
    lastSeen = lastSeen
)

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
    val metadata: Map<String, MetadataValue<Any>> = emptyMap()
)

data class FeatureUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val enabled: Boolean? = null,
    val flags: Map<String, Boolean>? = null,
    val metadata: Map<String, MetadataValue<Any>>? = null,
    val overrides: List<EnvironmentOverrideUpdateRequest>? = null
)

data class EnvironmentOverrideUpdateRequest(
    val environmentId: UUID,
    val enabled: Boolean? = null,
    val flags: Map<String, Boolean>? = null,
    val metadata: Map<String, MetadataValue<Any>>? = null,
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

data class EnvironmentRuleDto(
    val type: String,
    val config: Map<String, Any?>
)

fun EnvironmentRule.toDto() = EnvironmentRuleDto(
    type = type,
    config = config
)

fun EnvironmentRuleDto.toEntity() = EnvironmentRule(
    type = type,
    config = config
)

// Environment DTOs

data class EnvironmentDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val key: String,
    val sortOrder: Int,
    val rules: List<EnvironmentRuleDto>
)

fun EnvironmentEntity.toDto() = EnvironmentDto(
    id = this.id,
    name = this.name,
    key = this.key,
    description = this.description,
    sortOrder = this.sortOrder,
    rules = this.rules.map { it.toDto() }
)

data class EnvironmentCreateRequest(
    val name: String,
    val description: String?
)

data class EnvironmentUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val sortOrder: Int? = null,
    val rules: List<EnvironmentRuleDto>? = null
)

data class ClientPresenceDto(
    val environmentId: UUID,
    val environmentName: String,
    val lastSeen: java.time.Instant?,
    val technologies: Set<String>,
    val clientVersion: String?,
    val isOnline: Boolean
)

data class ClientRegistryDto(
    val id: UUID,
    val clientToken: String?,
    val clientName: String,
    val presences: List<ClientPresenceDto>,
    val featureUsage: List<FeatureUsageDto> = emptyList()
)

fun VividClientEntity.toDto(onlineThreshold: java.time.Duration, featureUsage: List<FeatureUsageEntity> = emptyList()): ClientRegistryDto = ClientRegistryDto(
    id = id,
    clientToken = clientToken,
    clientName = clientName,
    presences = presences.map { it.toDto(onlineThreshold) },
    featureUsage = featureUsage.map { it.toDto() }
)

fun VividClientPresenceEntity.toDto(onlineThreshold: java.time.Duration): ClientPresenceDto = ClientPresenceDto(
    environmentId = environment.id,
    environmentName = environment.name,
    lastSeen = lastSeen,
    technologies = technologies,
    clientVersion = clientVersion,
    isOnline = lastSeen.isAfter(java.time.Instant.now().minus(onlineThreshold))
)

data class ClientUpdateRequest(
    val clientName: String,
    val clientToken: String?
)

