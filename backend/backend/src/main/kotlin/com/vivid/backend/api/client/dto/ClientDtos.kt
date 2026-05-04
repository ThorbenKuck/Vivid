package com.vivid.backend.api.client.dto

import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.infrastructure.FeatureEntity
import com.vivid.backend.domain.entity.MetadataValue
import com.vivid.backend.domain.entity.VividClientEntity
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
        metadata = resolved.metadata,
    )
}

data class HeartbeatRequest(
    /**
     * A token, assigned by Vivid for this client.
     *
     * This token is only required if either multiple clients have the same name.
     * See the [com.vivid.backend.domain.entity.internal.SettingsEntity.requireClientTokens] field for more details.
     */
    val clientToken: String?,
    val applicationName: String,
    val environment: String,
    val technologies: Set<String>,
    val clientVersion: String?,
) {
    fun toEntity(getEnvironment: (String) -> EnvironmentEntity): VividClientEntity {
        return VividClientEntity(
            clientName = applicationName,
            clientToken = clientToken,
            environment = getEnvironment(environment),
            technologies = technologies.toMutableSet(),
            clientVersion = clientVersion,
            lastSeen = Instant.now(),
        )
    }
}

data class ClientRegistryDto(
    val id: UUID,
    val externalId: String,
    val clientName: String,
    val environmentId: UUID,
    val lastSeen: Instant,
    val technologies: Set<String>,
    val clientVersion: String,
    val isOnline: Boolean,
)
