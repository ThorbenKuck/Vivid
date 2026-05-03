package com.vivid.backend.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("permission")
class PermissionProperties(
    val rolePrefix: String = "vivid:",
    val adminRole: String = "admin",
    /**
     * Can be set explicitly to provide a "super user" role.
     *
     * If set, this role will be treated as a vivid administrator.
     * Can be used to reuse an existing and already existing role in Keycloak.
     */
    val rootRole: String? = null,
    /**
     * Default visibilities if a role is not explicitly set.
     */
    val defaultVisibility: Map<String, String> = mapOf(
        "environments" to "read"
    ),
)
