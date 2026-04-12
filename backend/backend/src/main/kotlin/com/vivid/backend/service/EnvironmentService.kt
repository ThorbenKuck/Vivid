package com.vivid.backend.service

import com.vivid.backend.api.web.dto.EnvironmentCreateRequest
import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.repository.EnvironmentRepository
import com.vivid.backend.service.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.UUID.fromString

@Service
class EnvironmentService(
    private val environmentRepository: EnvironmentRepository,
    private val departmentService: DepartmentService,
    private val permissionsService: PermissionService,
) {

    @Transactional(readOnly = true)
    fun search(q: String?, departmentId: UUID, pageable: Pageable): Page<EnvironmentEntity> {
        val environments = environmentRepository.findAll()
            .asSequence()
            .filter { it.department.id == departmentId }
            .filter { q.isNullOrBlank() || it.name.contains(q, ignoreCase = true) || it.description?.contains(q, ignoreCase = true) == true }
            .toList()

        val visibleEnvironments = permissionsService.filterVisibleEnvironments(environments)
        val start = pageable.offset.toInt().coerceAtMost(visibleEnvironments.size)
        val end = (start + pageable.pageSize).coerceAtMost(visibleEnvironments.size)
        val pageContent = if (start >= end) emptyList() else visibleEnvironments.subList(start, end)

        return PageImpl(pageContent, pageable, visibleEnvironments.size.toLong())
    }

    @Transactional(readOnly = true)
    fun requireEnvironment(id: String, departmentId: UUID): EnvironmentEntity {
        return findEnvironment(id, departmentId) ?: throw ResourceNotFoundException("Environment with id $id not found")
    }

    @Transactional(readOnly = true)
    fun findEnvironment(string: String, departmentId: UUID): EnvironmentEntity? {
        try {
            val uuid = fromString(string)
            return findEnvironment(uuid, departmentId)
        } catch (_: IllegalArgumentException) {
            return environmentRepository.findByNameAndDepartmentId(string, departmentId)
        }
    }

    @Transactional(readOnly = true)
    fun findEnvironment(uuid: UUID, departmentId: UUID): EnvironmentEntity? {
        val env = environmentRepository.findByIdOrNull(uuid)
        return if (env?.department?.id == departmentId) env else null
    }

    @Transactional(readOnly = true)
    fun findEnvironment(string: String): EnvironmentEntity? {
        try {
            val uuid = fromString(string)
            return environmentRepository.findByIdOrNull(uuid)
        } catch (_: IllegalArgumentException) {
            return environmentRepository.findByName(string)
        }
    }

    @Transactional(readOnly = true)
    fun getAll(departmentId: UUID): List<EnvironmentEntity> = environmentRepository.findAllByDepartmentId(departmentId)

    @Transactional
    fun create(departmentId: UUID, request: EnvironmentCreateRequest): EnvironmentEntity {
        if (environmentRepository.existsByNameIgnoreCaseAndDepartmentId(request.name, departmentId)) {
            throw IllegalArgumentException("Environment with name '${request.name}' already exists")
        }
        val department = departmentService.findById(departmentId)
        val e = EnvironmentEntity(name = request.name, description = request.description, department = department)
        return environmentRepository.save(e)
    }

    @Transactional
    fun delete(id: UUID, departmentId: UUID) {
        val env = findEnvironment(id, departmentId)
            ?: throw ResourceNotFoundException("Environment with id $id not found")
        environmentRepository.delete(env)
    }
}
