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
    private val userRepository: UserRepository
) {
    fun findById(id: UUID): User = userRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("User not found with id: $id") }

    fun findByKeycloakId(keycloakId: String): User? = userRepository.findByKeycloakId(keycloakId)

    fun syncUser(
        keycloakId: String,
        username: String,
        email: String,
        displayRole: String?,
    ): User {
        val existingUser = userRepository.findByKeycloakId(keycloakId)
        return if (existingUser != null) {
            existingUser.username = username
            existingUser.email = email
            existingUser.displayRole = displayRole
            userRepository.save(existingUser)
        } else {
            val newUser = User(
                keycloakId = keycloakId,
                username = username,
                email = email,
                displayRole = displayRole
            )
            userRepository.save(newUser)
        }
    }

    fun searchUsers(query: String): List<User> {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}
