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
    val description: String?,
    val environments: List<FeatureEnvironmentDto>,
    val tags: List<String>,
    val outgoingLinks: List<FeatureLinkDto>,
    val assignedTeams: List<TeamDto>
)

data class FeatureEnvironmentDto(
    val environmentId: UUID,
    val environmentName: String,
    val enabled: Boolean,
    val flags: Map<String, Boolean>,
    val metadata: Map<String, com.vivid.backend.domain.entity.MetadataValue>
)

fun FeatureEnvironment.toDto(): FeatureEnvironmentDto = FeatureEnvironmentDto(
    environmentId = environment.id!!,
    environmentName = environment.name,
    enabled = enabled,
    flags = flags.toMap(),
    metadata = metadata.toMap()
)

fun Feature.toDto(allEnvironments: List<EnvironmentEntity>): FeatureDto {
    val envDtos = allEnvironments.map { env ->
        val fe = findFeatureEnvironment(env)
        FeatureEnvironmentDto(
            environmentId = env.id!!,
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
        description = description,
        environments = envDtos,
        tags = tags.toList(),
        outgoingLinks = outgoingLinks.map { it.toDto() },
        assignedTeams = assignedTeams.map { it.toDto() }
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
    val environments: List<FeatureEnvironmentUpdateRequest>? = null,
    val assignedTeamIds: List<UUID>? = null
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

fun FeatureCreateRequest.toEntity(department: com.vivid.backend.domain.entity.Department): Feature = Feature(
    name = this.name,
    description = this.description,
    tags = this.tags.toMutableSet(),
    department = department
)

// Environment DTOs

data class EnvironmentDto(
    val id: java.util.UUID?,
    val name: String,
    val description: String?
)

fun EnvironmentEntity.toDto() = EnvironmentDto(id = this.id, name = this.name, description = this.description)

data class EnvironmentCreateRequest(
    val name: String,
    val description: String?
)

// Department DTOs

data class DepartmentDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val teams: List<TeamDto>? = null
)

fun com.vivid.backend.domain.entity.Department.toDto(includeTeams: Boolean = false): DepartmentDto = DepartmentDto(
    id = id,
    name = name,
    description = description,
    teams = if (includeTeams) teams.map { it.toDto(includeMembers = true) } else null
)

data class DepartmentCreateRequest(
    val name: String,
    val description: String? = null
)
