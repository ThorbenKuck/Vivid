package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.UserDto
import com.vivid.backend.api.web.dto.UserSyncRequest
import com.vivid.backend.api.web.dto.toDto
import com.vivid.backend.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/web/users")
@Tag(name = "Web User Management", description = "Manage users and synchronization")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/sync")
    @Operation(summary = "Synchronize user from Keycloak (JIT Provisioning)")
    fun syncUser(@RequestBody request: UserSyncRequest): UserDto {
        return userService.syncUser(
            keycloakId = request.keycloakId,
            username = request.username,
            email = request.email,
            displayRole = request.displayRole
        ).toDto()
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by username or email")
    fun searchUsers(@RequestParam q: String): List<UserDto> {
        return userService.searchUsers(q).map { it.toDto() }
    }

    @GetMapping
    @Operation(summary = "Get all users")
    fun getAllUsers(): List<UserDto> {
        return userService.getAllUsers().map { it.toDto() }
    }
}
