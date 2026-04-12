package com.vivid.backend.api.web.dto

data class PermissionSetDto(
    val admin: Boolean = false,
    val environments: String = "none", // "none", "read", "write"
    val teams: String = "none",
    val departments: String = "none",
    val environment: EnvironmentPermissionsDto = EnvironmentPermissionsDto(),
    val resolved: Boolean = true,
)

data class EnvironmentPermissionsDto(
    val admin: Boolean = false,
    val all: String = "none", // "none", "read", "write"
    val specific: Map<String, String> = emptyMap() // Map<envId, "none"|"read"|"write">
)
