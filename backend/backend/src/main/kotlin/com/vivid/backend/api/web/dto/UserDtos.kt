package com.vivid.backend.api.web.dto

import com.vivid.backend.domain.entity.User
import java.util.*

data class UserDto(
    val id: UUID,
    val username: String,
    val email: String,
    val displayRole: String?
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    username = username,
    email = email,
    displayRole = displayRole
)

data class UserSyncRequest(
    val keycloakId: String,
    val username: String,
    val email: String,
    val displayRole: String?
)
