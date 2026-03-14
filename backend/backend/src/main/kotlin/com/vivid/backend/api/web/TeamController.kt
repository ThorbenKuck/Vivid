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
        @RequestParam(required = false) q: String?,
        pageable: Pageable
    ): Page<TeamDto> {
        return if (q != null) {
            teamService.searchTeams(q, pageable).map { it.toDto() }
        } else {
            teamService.getAllTeams(pageable).map { it.toDto() }
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID")
    fun getTeamById(@PathVariable id: UUID): TeamDto {
        return teamService.getTeamById(id).toDto(includeMembers = true)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new team")
    fun createTeam(@RequestBody request: TeamCreateRequest): TeamDto {
        return teamService.createTeam(request.name, request.description).toDto()
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing team")
    fun updateTeam(@PathVariable id: UUID, @RequestBody request: TeamUpdateRequest): TeamDto {
        return teamService.updateTeam(id, request.name, request.description).toDto(includeMembers = true)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a team")
    fun deleteTeam(@PathVariable id: UUID) {
        teamService.deleteTeam(id)
    }

    @PostMapping("/{id}/members/{userId}")
    @Operation(summary = "Add a member to a team")
    fun addMember(@PathVariable id: UUID, @PathVariable userId: UUID): TeamDto {
        teamService.addMember(id, userId)
        return teamService.getTeamById(id).toDto(includeMembers = true)
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove a member from a team")
    fun removeMember(@PathVariable id: UUID, @PathVariable userId: UUID): TeamDto {
        teamService.removeMember(id, userId)
        return teamService.getTeamById(id).toDto(includeMembers = true)
    }
}
