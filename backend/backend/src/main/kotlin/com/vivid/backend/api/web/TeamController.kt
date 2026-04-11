package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.*
import com.vivid.backend.service.TeamService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/web/teams")
@Tag(name = "Web Team Management", description = "CRUD operations for teams")
class TeamController(
    private val teamService: TeamService
) {
    @GetMapping
    @Operation(summary = "Get all teams (paginated)")
    fun getAllTeams(
        @RequestParam departmentId: UUID,
        @RequestParam(required = false) q: String?,
        pageable: Pageable
    ): Page<TeamDto> {
        return if (q != null) {
            teamService.searchTeams(q, departmentId, pageable).map { it.toDto() }
        } else {
            teamService.getAllTeams(departmentId, pageable).map { it.toDto() }
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID")
    fun getTeamById(
        @PathVariable id: UUID,
        @RequestParam departmentId: UUID
    ): TeamDto {
        return teamService.getTeamById(id, departmentId).toDto(includeMembers = true)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new team")
    fun createTeam(
        @RequestParam departmentId: UUID,
        @RequestBody request: TeamCreateRequest
    ): TeamDto {
        return teamService.createTeam(departmentId, request.name, request.description).toDto()
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing team")
    fun updateTeam(
        @PathVariable id: UUID,
        @RequestParam departmentId: UUID,
        @RequestBody request: TeamUpdateRequest
    ): TeamDto {
        return teamService.updateTeam(id, departmentId, request.name, request.description).toDto(includeMembers = true)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a team")
    fun deleteTeam(
        @PathVariable id: UUID,
        @RequestParam departmentId: UUID
    ) {
        teamService.deleteTeam(id, departmentId)
    }

    @PostMapping("/{id}/members/{userId}")
    @Operation(summary = "Add a member to a team")
    fun addMember(
        @PathVariable id: UUID,
        @PathVariable userId: UUID,
        @RequestParam departmentId: UUID
    ): TeamDto {
        teamService.addMember(id, userId, departmentId)
        return teamService.getTeamById(id, departmentId).toDto(includeMembers = true)
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove a member from a team")
    fun removeMember(
        @PathVariable id: UUID,
        @PathVariable userId: UUID,
        @RequestParam departmentId: UUID
    ): TeamDto {
        teamService.removeMember(id, userId, departmentId)
        return teamService.getTeamById(id, departmentId).toDto(includeMembers = true)
    }
}
