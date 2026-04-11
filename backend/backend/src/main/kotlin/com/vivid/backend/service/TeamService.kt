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
    private val userService: UserService,
    private val departmentService: DepartmentService
) {
    fun getAllTeams(departmentId: UUID, pageable: Pageable): Page<Team> {
        return teamRepository.findAllByDepartmentId(departmentId, pageable)
    }

    fun getTeamById(id: UUID, departmentId: UUID): Team {
        val team = teamRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Team not found with id: $id") }
        if (team.department.id != departmentId) {
            throw ResourceNotFoundException("Team not found in this department")
        }
        return team
    }

    fun createTeam(departmentId: UUID, name: String, description: String?): Team {
        val department = departmentService.findById(departmentId)
        val team = Team(name = name, description = description, department = department)
        return teamRepository.save(team)
    }

    fun updateTeam(id: UUID, departmentId: UUID, name: String?, description: String?): Team {
        val team = getTeamById(id, departmentId)
        name?.let { team.name = it }
        description?.let { team.description = it }
        return teamRepository.save(team)
    }

    fun deleteTeam(id: UUID, departmentId: UUID) {
        val team = getTeamById(id, departmentId)
        teamRepository.delete(team)
    }

    fun addMember(teamId: UUID, userId: UUID, departmentId: UUID) {
        val team = getTeamById(teamId, departmentId)
        val user = userService.findById(userId, departmentId)
        team.members.add(user)
        teamRepository.save(team)
    }

    fun removeMember(teamId: UUID, userId: UUID, departmentId: UUID) {
        val team = getTeamById(teamId, departmentId)
        val user = userService.findById(userId, departmentId)
        team.members.remove(user)
        teamRepository.save(team)
    }

    fun searchTeams(query: String, departmentId: UUID, pageable: Pageable): Page<Team> {
        return teamRepository.findByNameContainingIgnoreCaseAndDepartmentId(query, departmentId, pageable)
    }
}
