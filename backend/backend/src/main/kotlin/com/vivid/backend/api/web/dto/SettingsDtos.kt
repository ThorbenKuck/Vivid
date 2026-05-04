package com.vivid.backend.api.web.dto

data class DistributionProviderDto(
    val name: String,
    val type: String,
    val status: String,
    val details: Map<String, String> = emptyMap()
)
