package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.UserDto
import com.vivid.backend.api.web.dto.UserSyncRequest
import com.vivid.backend.api.web.dto.toDto
import com.vivid.backend.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/web/users")
@Tag(name = "Web User Management", description = "Manage users and synchronization")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/sync")
    @Operation(summary = "Synchronize user from Keycloak (JIT Provisioning)")
    fun syncUser(
        @RequestParam(required = false) departmentId: UUID?,
        @RequestBody request: UserSyncRequest
    ): UserDto {
        return userService.syncUser(
            keycloakId = request.keycloakId,
            username = request.username,
            email = request.email,
            displayRole = request.displayRole,
            departmentId = departmentId
        ).toDto()
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by username or email")
    fun searchUsers(
        @RequestParam q: String,
        @RequestParam departmentId: UUID
    ): List<UserDto> {
        return userService.searchUsers(q, departmentId).map { it.toDto() }
    }

    @GetMapping
    @Operation(summary = "Get all users")
    fun getAllUsers(@RequestParam departmentId: UUID): List<UserDto> {
        return userService.getAllUsers(departmentId).map { it.toDto() }
    }
}
