package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface UserRepository : JpaRepository<User, UUID> {
    fun findByKeycloakId(keycloakId: String): User?

    @Query("select u from User u where (lower(u.username) like lower(concat('%', :query, '%')) or lower(u.email) like lower(concat('%', :query, '%'))) and u.department.id = :departmentId")
    fun search(query: String, departmentId: UUID): List<User>

    fun findAllByDepartmentId(departmentId: UUID): List<User>
}
