package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.AuthConfigDto
import com.vivid.backend.api.web.dto.PermissionSetDto
import com.vivid.backend.service.PermissionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val permissionService: PermissionService,
    @Value("\${vivid.auth.issuer:}") private val issuer: String,
    @Value("\${vivid.auth.client-id:}") private val clientId: String,
    @Value("\${vivid.auth.frontend-base-url:}") private val frontendBaseUrl: String
) {
    @GetMapping("/config")
    fun getConfig(): AuthConfigDto {
        val logoutUrl = if (issuer.isNotBlank() && clientId.isNotBlank()) {
            "$issuer/protocol/openid-connect/logout?client_id=$clientId&post_logout_redirect_uri=$frontendBaseUrl/login"
        } else null

        return AuthConfigDto(
            issuer = issuer.ifBlank { null },
            logoutUrl = logoutUrl
        )
    }

    @GetMapping("/permissions")
    fun getPermissions(): PermissionSetDto {
        return permissionService.getEffectivePermissions()
    }
}