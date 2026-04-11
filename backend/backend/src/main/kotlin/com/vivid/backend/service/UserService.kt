package com.vivid.backend.service

import com.vivid.backend.domain.entity.User
import com.vivid.backend.domain.repository.UserRepository
import com.vivid.backend.service.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val departmentRepository: com.vivid.backend.domain.repository.DepartmentRepository
) {
    fun findById(id: UUID, departmentId: UUID): User {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User not found with id: $id") }
        if (user.department.id != departmentId) {
            throw ResourceNotFoundException("User not found in this department")
        }
        return user
    }

    fun findById(id: UUID): User = userRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("User not found with id: $id") }

    fun findByKeycloakId(keycloakId: String): User? = userRepository.findByKeycloakId(keycloakId)

    fun syncUser(
        keycloakId: String,
        username: String,
        email: String,
        displayRole: String?,
        departmentId: UUID? = null
    ): User {
        val existingUser = userRepository.findByKeycloakId(keycloakId)
        return if (existingUser != null) {
            existingUser.username = username
            existingUser.email = email
            existingUser.displayRole = displayRole
            userRepository.save(existingUser)
        } else {
            val department = if (departmentId != null) {
                departmentRepository.findById(departmentId).orElseThrow { ResourceNotFoundException("Department not found") }
            } else {
                // Fallback to General department
                departmentRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .orElseThrow { ResourceNotFoundException("General department not found") }
            }
            val newUser = User(
                keycloakId = keycloakId,
                username = username,
                email = email,
                displayRole = displayRole,
                department = department
            )
            userRepository.save(newUser)
        }
    }

    fun searchUsers(query: String, departmentId: UUID): List<User> {
        return userRepository.search(query, departmentId)
    }

    fun getAllUsers(departmentId: UUID): List<User> {
        return userRepository.findAllByDepartmentId(departmentId)
    }
}
