package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.EnvironmentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface EnvironmentRepository : JpaRepository<EnvironmentEntity, UUID> {

    fun existsByNameIgnoreCase(name: String): Boolean

    @Query(
        """
        select e from Environment e
        where e.department.id = :departmentId
        and (:q is null or :q = '' or lower(e.name) like lower(concat('%', :q, '%')))
        """
    )
    fun search(q: String?, departmentId: UUID, pageable: Pageable): Page<EnvironmentEntity>

    fun findAllByDepartmentId(departmentId: UUID): List<EnvironmentEntity>
    fun findByName(name: String): EnvironmentEntity?
    fun findByNameAndDepartmentId(name: String, departmentId: UUID): EnvironmentEntity?
    fun existsByNameIgnoreCaseAndDepartmentId(name: String, departmentId: UUID): Boolean
}