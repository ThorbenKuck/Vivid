package com.vivid.backend.api.web.dto

import com.vivid.backend.domain.entity.Team
import java.util.*

data class TeamDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val memberCount: Int,
    val members: List<UserDto>? = null
)

fun Team.toDto(includeMembers: Boolean = false): TeamDto = TeamDto(
    id = id,
    name = name,
    description = description,
    memberCount = members.size,
    members = if (includeMembers) members.map { it.toDto() } else null
)

data class TeamCreateRequest(
    val name: String,
    val description: String? = null
)

data class TeamUpdateRequest(
    val name: String?,
    val description: String?
)
