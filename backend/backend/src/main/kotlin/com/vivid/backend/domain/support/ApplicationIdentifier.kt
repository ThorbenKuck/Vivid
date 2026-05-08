package com.vivid.backend.domain.support

import com.vivid.backend.domain.entity.EnvironmentEntity

data class ApplicationIdentifier(
    val applicationId: String,
    val environment: EnvironmentEntity,
    val token: String?,
)
