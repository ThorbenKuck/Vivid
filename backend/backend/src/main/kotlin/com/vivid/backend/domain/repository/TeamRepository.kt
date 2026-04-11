package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.Team
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TeamRepository : JpaRepository<Team, UUID> {
    fun findByNameContainingIgnoreCaseAndDepartmentId(name: String, departmentId: UUID, pageable: Pageable): Page<Team>
    fun findAllByDepartmentId(departmentId: UUID, pageable: Pageable): Page<Team>
}
