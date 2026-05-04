package com.vivid.backend.api.web.dto

import com.vivid.backend.domain.entity.internal.SettingsEntity
import java.time.Duration

data class SettingsDto(
    val requireClientTokens: Boolean,
    val allowDynamicClientRegistration: Boolean,
    val onlineThreshold: Duration = Duration.ofMinutes(5),
)

fun SettingsEntity.toDto(): SettingsDto {
    return SettingsDto(
        requireClientTokens = requireClientTokens,
        allowDynamicClientRegistration = allowDynamicClientRegistration,
        onlineThreshold = onlineThreshold,
    )
}
