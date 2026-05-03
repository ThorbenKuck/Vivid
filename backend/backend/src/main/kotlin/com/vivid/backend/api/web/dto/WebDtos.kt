package com.vivid.backend.api.web.dto

import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.Feature
import com.vivid.backend.domain.entity.FeatureEnvironment
import com.vivid.backend.domain.entity.FeatureLink
import java.util.*

// Feature DTO represents a feature with environment-specific state (enabled/flags/metadata)
// for a given environment context.

data class FeatureDto(
    val id: UUID?,
    val runningNumber: Long,
    val name: String,
    val key: String,
    val description: String?,
    val environments: List<FeatureEnvironmentDto>,
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

fun com.vivid.backend.domain.entity.Note.toDto(): NoteDto = NoteDto(
    id = id,
    content = content,
    authorName = author.username,
    timestamp = timestamp
)

data class FeatureEnvironmentDto(
    val environmentId: UUID,
    val environmentName: String,
    val enabled: Boolean,
    val flags: Map<String, Boolean>,
    val metadata: Map<String, com.vivid.backend.domain.entity.MetadataValue>
)

fun FeatureEnvironment.toDto(): FeatureEnvironmentDto = FeatureEnvironmentDto(
    environmentId = environment.id,
    environmentName = environment.name,
    enabled = enabled,
    flags = flags.toMap(),
    metadata = metadata.toMap()
)

fun Feature.toDto(allEnvironments: List<EnvironmentEntity>): FeatureDto {
    val envDtos = allEnvironments.map { env ->
        val fe = findFeatureEnvironment(env)
        FeatureEnvironmentDto(
            environmentId = env.id,
            environmentName = env.name,
            enabled = fe?.enabled ?: false,
            flags = fe?.flags ?: emptyMap(),
            metadata = fe?.metadata ?: emptyMap()
        )
    }
    return FeatureDto(
        id = id,
        runningNumber = runningNumber,
        name = name,
        key = key,
        description = description,
        environments = envDtos,
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
    val tags: List<String> = emptyList()
)

data class FeatureUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val environments: List<FeatureEnvironmentUpdateRequest>? = null
)

data class NoteCreateRequest(
    val content: String
)

data class FeatureEnvironmentUpdateRequest(
    val environmentId: UUID,
    val enabled: Boolean = false,
    val flags: Map<String, Boolean> = emptyMap(),
    val metadata: Map<String, com.vivid.backend.domain.entity.MetadataValue> = emptyMap()
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

