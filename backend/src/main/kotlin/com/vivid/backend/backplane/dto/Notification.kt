package com.vivid.backend.backplane.dto

import java.util.UUID

data class Notification(
    val environmentIds: List<UUID>?,
    val featureId: UUID,
    val instanceId: String?,
)
