package com.vivid.backend.service

import com.vivid.backend.api.web.dto.EnvironmentPermissionsDto
import com.vivid.backend.api.web.dto.PermissionSetDto
import com.vivid.backend.domain.entity.EnvironmentEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class PermissionService(
    private val properties: PermissionProperties,
) {

    private val adminRole = properties.rolePrefix + properties.adminRole
    private val superUserPermissions = PermissionSetDto(
        admin = true,
        environments = "write",
        environment = EnvironmentPermissionsDto(admin = true, all = "write")
    )

    fun filterVisibleEnvironments(environments: List<EnvironmentEntity>): List<EnvironmentEntity> {
        val perms = getEffectivePermissions()
        if (perms.admin || perms.environment.admin) return environments

        val allLevel = perms.environment.all
        if (allLevel == "read" || allLevel == "write") return environments

        return environments.filter { env ->
            val specificLevel = perms.environment.specific[env.name] ?: "none"
            specificLevel == "read" || specificLevel == "write"
        }
    }

    fun getEffectivePermissions(): PermissionSetDto {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication !is JwtAuthenticationToken) {
            return PermissionSetDto(resolved = false)
        }

        val jwt = authentication.token
        val roles = extractRoles(jwt)

        if (properties.rootRole != null && roles.contains(properties.rootRole)) {
            return superUserPermissions
        }

        if (roles.contains(adminRole)) {
            return superUserPermissions
        }

        val envAdmin = roles.contains("${properties.rolePrefix}env:admin")
        val envAllWrite = envAdmin || roles.contains("${properties.rolePrefix}env:all:write")
        val envAllRead = envAllWrite || roles.contains("${properties.rolePrefix}env:all:read")

        val specificEnv = mutableMapOf<String, String>()
        roles.forEach { role ->
            if (role.startsWith("${properties.rolePrefix}env:")) {
                val parts = role.split(":")
                if (parts.size == 3) {
                    val envId = parts[1]
                    val action = parts[2] // "read" or "write"
                    if (envId != "all" && envId != "admin") {
                        val current = specificEnv[envId]
                        if (current != "write") {
                            specificEnv[envId] = action
                        }
                    }
                }
            }
        }

        return PermissionSetDto(
            admin = false,
            environments = getAccessLevel(roles, "environments"),
            environment = EnvironmentPermissionsDto(
                admin = envAdmin,
                all = if (envAllWrite) "write" else if (envAllRead) "read" else "none",
                specific = specificEnv
            )
        )
    }

    private fun getAccessLevel(roles: Collection<String>, resource: String): String {
        return when {
            roles.contains("${properties.rolePrefix}$resource:write") -> "write"
            roles.contains("$resource:read") -> "read"
            else -> properties.defaultVisibility[resource] ?: "none"
        }
    }

    private fun extractRoles(jwt: Jwt): Set<String> {
        val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access")
        val roles = realmAccess?.get("roles") as? List<String> ?: emptyList()
        return roles.toSet()
    }

    fun hasPermission(resource: String, action: String): Boolean {
        val perms = getEffectivePermissions()
        if (perms.admin) return true

        val level = when (resource) {
            "environments" -> perms.environments
            else -> "none"
        }

        return checkAccess(level, action)
    }

    fun hasEnvPermission(envId: String, action: String): Boolean {
        val perms = getEffectivePermissions()
        if (perms.admin || perms.environment.admin) return true

        val allLevel = perms.environment.all
        if (checkAccess(allLevel, action)) return true

        val specificLevel = perms.environment.specific[envId] ?: "none"
        return checkAccess(specificLevel, action)
    }

    private fun checkAccess(level: String, action: String): Boolean {
        return when (action) {
            "write" -> level == "write"
            "read" -> level == "write" || level == "read"
            else -> false
        }
    }
}
