package com.vivid.backend.service

import com.vivid.backend.domain.entity.Team
import com.vivid.backend.domain.entity.User
import com.vivid.backend.domain.repository.TeamRepository
import com.vivid.backend.service.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class TeamService(
    private val teamRepository: TeamRepository,
    private val userService: UserService
) {
    fun getAllTeams(pageable: Pageable): Page<Team> {
        return teamRepository.findAll(pageable)
    }

    fun getTeamById(id: UUID): Team = teamRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("Team not found with id: $id") }

    fun createTeam(name: String, description: String?): Team {
        val team = Team(name = name, description = description)
        return teamRepository.save(team)
    }

    fun updateTeam(id: UUID, name: String?, description: String?): Team {
        val team = getTeamById(id)
        name?.let { team.name = it }
        description?.let { team.description = it }
        return teamRepository.save(team)
    }

    fun deleteTeam(id: UUID) {
        teamRepository.deleteById(id)
    }

    fun addMember(teamId: UUID, userId: UUID) {
        val team = getTeamById(teamId)
        val user = userService.findById(userId)
        team.members.add(user)
        teamRepository.save(team)
    }

    fun removeMember(teamId: UUID, userId: UUID) {
        val team = getTeamById(teamId)
        val user = userService.findById(userId)
        team.members.remove(user)
        teamRepository.save(team)
    }

    fun searchTeams(query: String, pageable: Pageable): Page<Team> {
        return teamRepository.findByNameContainingIgnoreCase(query, pageable)
    }
}
