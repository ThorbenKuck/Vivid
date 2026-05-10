package com.vivid.backend.api.web.dto

data class PermissionSetDto(
    val admin: Boolean = false,
    val environments: String = "none", // "none", "read", "write"
    val environment: EnvironmentPermissionsDto = EnvironmentPermissionsDto(),
    val clients: String = "none", // "none", "read", "write"
    val settings: String = "none", // "none", "read", "write"
    val features: String = "none", // "none", "read", "write"
    val resolved: Boolean = true,
)

data class EnvironmentPermissionsDto(
    val admin: Boolean = false,
    val all: String = "none", // "none", "read", "write"
    val specific: Map<String, String> = emptyMap() // Map<envId, "none"|"read"|"write">
)
