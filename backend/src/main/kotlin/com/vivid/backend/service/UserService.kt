package com.vivid.backend.service

import com.vivid.backend.domain.entity.User
import com.vivid.backend.domain.repository.UserRepository
import com.vivid.backend.service.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = LoggerFactory.getLogger(UserService::class.java)

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {
    fun findById(id: UUID): User = userRepository.findById(id)
        .orElseThrow { ResourceNotFoundException("User not found with id: $id") }

    fun findByKeycloakId(keycloakId: String): User? = userRepository.findByKeycloakId(keycloakId)

    fun syncUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication !is JwtAuthenticationToken) {
            throw ResourceNotFoundException("User not authenticated!")
        }

        val jwt = authentication.token
        val sub = jwt.subject
        val username = jwt.getClaimAsString("preferred_username") ?: sub
        val email = jwt.getClaimAsString("email") ?: ""
        val vividRole = jwt.getClaimAsString("vivid_role")
        val name = jwt.getClaimAsString("name")

        return syncUser(
            keycloakId = sub,
            username = username,
            name = name,
            email = email,
            displayRole = vividRole
        )
    }

    fun syncUser(
        keycloakId: String,
        username: String,
        name: String?,
        email: String?,
        displayRole: String?
    ): User {
        val existingUser = userRepository.findByKeycloakId(keycloakId)
        return if (existingUser != null) {
            logger.trace("Updating user with keycloakId: $keycloakId")
            existingUser.username = username
            existingUser.email = email
            existingUser.displayRole = displayRole
            existingUser.name = name
            userRepository.save(existingUser)
        } else {
            logger.info("Creating new user with keycloakId: $keycloakId")
            val newUser = User(
                keycloakId = keycloakId,
                username = username,
                name = name,
                email = email,
                displayRole = displayRole
            )
            userRepository.save(newUser)
        }
    }

    fun searchUsers(query: String): List<User> {
        return userRepository.search(query)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}
