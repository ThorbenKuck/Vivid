package com.vivid.backend.clients.streams.dto

import java.util.UUID

data class FeatureChangedEvent(
    val featureId: UUID,
    val environmentIds: List<UUID>? = null
)