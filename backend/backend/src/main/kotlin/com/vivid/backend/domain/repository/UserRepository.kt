package com.vivid.backend.domain.repository

import com.vivid.backend.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, UUID> {
    fun findByKeycloakId(keycloakId: String): User?
    fun findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(username: String, email: String): List<User>
}
