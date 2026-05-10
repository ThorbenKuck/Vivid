package com.vivid.backend.api.web

import com.vivid.backend.ApplicationProperties
import com.vivid.backend.api.web.dto.AuthConfigDto
import com.vivid.backend.api.web.dto.PermissionSetDto
import com.vivid.backend.service.PermissionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val permissionService: PermissionService,
    private val properties: ApplicationProperties,
) {
    @GetMapping("/config")
    fun getConfig(): AuthConfigDto {
        return AuthConfigDto(
            issuer = properties.oidc.issuerUrl,
            issuerName = properties.oidc.issuerName,
            logoutUrl = properties.oidc.calculateLogoutUrl()
        )
    }

    @GetMapping("/permissions")
    fun getPermissions(): PermissionSetDto {
        return permissionService.getEffectivePermissions()
    }
}