package com.vivid.backend.service

import com.vivid.backend.api.web.dto.EnvironmentCreateRequest
import com.vivid.backend.api.web.dto.EnvironmentUpdateRequest
import com.vivid.backend.api.web.dto.toEntity
import com.vivid.backend.asKey
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
    private val permissionsService: PermissionService,
) {

    @Transactional(readOnly = true)
    fun search(q: String?, pageable: Pageable): Page<EnvironmentEntity> {
        val environments = environmentRepository.findAll()
            .asSequence()
            .filter {
                q.isNullOrBlank() || it.name.contains(q, ignoreCase = true) || it.description?.contains(
                    q,
                    ignoreCase = true
                ) == true
            }
            .sortedBy { it.sortOrder }
            .toList()

        val visibleEnvironments = permissionsService.filterVisibleEnvironments(environments)
        val start = pageable.offset.toInt().coerceAtMost(visibleEnvironments.size)
        val end = (start + pageable.pageSize).coerceAtMost(visibleEnvironments.size)
        val pageContent = if (start >= end) emptyList() else visibleEnvironments.subList(start, end)

        return PageImpl(pageContent, pageable, visibleEnvironments.size.toLong())
    }

    @Transactional(readOnly = true)
    fun requireEnvironment(id: String): EnvironmentEntity {
        return findEnvironment(id) ?: throw ResourceNotFoundException("Environment with id $id not found")
    }

    @Transactional(readOnly = true)
    fun findEnvironment(string: String): EnvironmentEntity? {
        return environmentRepository.findByKey(string)
            ?: string.toUuid()?.let { environmentRepository.findByIdOrNull(it) }
            ?: environmentRepository.findByName(string)
    }

    private fun String.toUuid(): UUID? {
        return try {
            fromString(this)
        } catch (_: IllegalArgumentException) {
            return null
        }
    }

    @Transactional(readOnly = true)
    fun getAll(): List<EnvironmentEntity> = environmentRepository.findAll().sortedBy { it.sortOrder }

    @Transactional
    fun create(request: EnvironmentCreateRequest): EnvironmentEntity {
        if (environmentRepository.existsByNameIgnoreCase(request.name)) {
            throw IllegalArgumentException("Environment with name '${request.name}' already exists")
        }

        val maxSortOrder = environmentRepository.findAll().maxOfOrNull { it.sortOrder } ?: -1

        val e = EnvironmentEntity(
            name = request.name,
            description = request.description,
            key = request.name.determineKey(),
            sortOrder = maxSortOrder + 1
        )
        return environmentRepository.save(e)
    }

    @Transactional
    fun update(id: UUID, request: EnvironmentUpdateRequest): EnvironmentEntity {
        val env = environmentRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("Environment with id $id not found")

        request.name?.let {
            if (it != env.name && environmentRepository.existsByNameIgnoreCase(it)) {
                throw IllegalArgumentException("Environment with name '$it' already exists")
            }
            env.name = it
            env.key = it.determineKey()
        }
        request.description?.let { env.description = it }
        request.sortOrder?.let { env.sortOrder = it }
        request.rules?.let { rules ->
            env.rules = rules.map { it.toEntity() }
        }

        return environmentRepository.save(env)
    }

    @Transactional
    fun reorder(ids: List<UUID>): List<EnvironmentEntity> {
        val environments = environmentRepository.findAllById(ids)
        ids.forEachIndexed { index, uuid ->
            environments.find { it.id == uuid }?.let {
                it.sortOrder = index
            }
        }
        return environmentRepository.saveAll(environments)
    }

    @Transactional
    fun delete(id: UUID) {
        val env = environmentRepository.findByIdOrNull(id)
            ?: throw ResourceNotFoundException("Environment with id $id not found")
        environmentRepository.delete(env)
    }

    private fun String.determineKey(): String {
        val key = asKey()
        environmentRepository.findByKey(key) ?: return key

        var suffix = 0
        var keyWithSuffix = key + suffix
        while (environmentRepository.findByKey(keyWithSuffix) != null) {
            ++suffix
            keyWithSuffix = key + suffix
        }

        return keyWithSuffix
    }
}
