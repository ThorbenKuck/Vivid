package com.vivid.backend.service

import com.vivid.backend.api.web.dto.PermissionSetDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class PermissionServiceTest {

    private lateinit var permissionService: PermissionService
    private val properties = PermissionProperties(
        rolePrefix = "vivid:",
        adminRole = "admin",
        rootRole = "root",
        defaultVisibility = mapOf("environments" to "read")
    )

    @BeforeEach
    fun setup() {
        permissionService = PermissionService(properties)
    }

    private fun mockJwt(roles: List<String>) {
        val jwt = mock<Jwt> {
            on { getClaim<Map<String, Any>>("realm_access") } doReturn mapOf("roles" to roles)
        }
        
        val auth = JwtAuthenticationToken(jwt)
        val securityContext = mock<SecurityContext> {
            on { authentication } doReturn auth
        }
        SecurityContextHolder.setContext(securityContext)
    }

    @Test
    fun `should grant all to root role`() {
        mockJwt(listOf("root"))
        val perms = permissionService.getEffectivePermissions()
        assertTrue(perms.admin)
        assertEquals("write", perms.clients)
        assertEquals("write", perms.settings)
        assertEquals("write", perms.features)
    }

    @Test
    fun `should map new roles correctly`() {
        mockJwt(listOf("vivid:clients:read", "vivid:settings:write", "vivid:features:read"))
        val perms = permissionService.getEffectivePermissions()
        assertEquals("read", perms.clients)
        assertEquals("write", perms.settings)
        assertEquals("read", perms.features)
    }

    @Test
    fun `should use default visibility when no roles found`() {
        mockJwt(emptyList())
        val perms = permissionService.getEffectivePermissions()
        assertEquals("none", perms.clients)
        assertEquals("none", perms.settings)
        assertEquals("none", perms.features)
        assertEquals("read", perms.environments)
    }

    @Test
    fun `hasPermission should return true for admin`() {
        mockJwt(listOf("root"))
        assertTrue(permissionService.hasPermission("clients", "write"))
        assertTrue(permissionService.hasPermission("settings", "read"))
    }

    @Test
    fun `hasPermission should return true if level matches`() {
        mockJwt(listOf("vivid:clients:read", "vivid:settings:write"))
        assertTrue(permissionService.hasPermission("clients", "read"))
        org.junit.jupiter.api.Assertions.assertFalse(permissionService.hasPermission("clients", "write"))
        assertTrue(permissionService.hasPermission("settings", "write"))
        assertTrue(permissionService.hasPermission("settings", "read"))
    }
}
