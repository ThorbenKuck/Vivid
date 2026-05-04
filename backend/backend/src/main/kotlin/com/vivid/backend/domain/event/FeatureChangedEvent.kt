package com.vivid.backend.domain.event

import java.util.*

data class FeatureChangedEvent(
    val featureId: UUID,
    val environmentIds: List<UUID>? = null
)
