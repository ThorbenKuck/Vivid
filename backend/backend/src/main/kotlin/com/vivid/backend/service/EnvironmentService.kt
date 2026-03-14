package com.vivid.backend.service

import com.vivid.backend.api.web.dto.EnvironmentCreateRequest
import com.vivid.backend.domain.entity.Environment
import com.vivid.backend.domain.repository.EnvironmentRepository
import com.vivid.backend.service.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.UUID.fromString

@Service
class EnvironmentService(
    private val environmentRepository: EnvironmentRepository
) {

    @Transactional(readOnly = true)
    fun search(q: String?, pageable: Pageable): Page<Environment> {
        return environmentRepository.search(q, pageable)
    }

    @Transactional(readOnly = true)
    fun requireEnvironment(id: String): Environment {
        return findEnvironment(id) ?: throw ResourceNotFoundException("Environment with id $id not found")
    }

    @Transactional(readOnly = true)
    fun findEnvironment(string: String): Environment? {
        try {
            val uuid = fromString(string)
            return findEnvironment(uuid)
        } catch (_: IllegalArgumentException) {
            return environmentRepository.findByName(string)
        }
    }

    @Transactional(readOnly = true)
    fun findEnvironment(uuid: UUID): Environment? {
        return environmentRepository.findByIdOrNull(uuid)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<Environment> = environmentRepository.findAll()

    @Transactional
    fun create(request: EnvironmentCreateRequest): Environment {
        if (environmentRepository.existsByNameIgnoreCase(request.name)) {
            throw IllegalArgumentException("Environment with name '${request.name}' already exists")
        }
        val e = Environment(name = request.name, description = request.description)
        return environmentRepository.save(e)
    }

    @Transactional
    fun delete(id: UUID) {
        if (!environmentRepository.existsById(id)) {
            throw ResourceNotFoundException("Environment with id $id not found")
        }
        environmentRepository.deleteById(id)
    }
}
