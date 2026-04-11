package com.vivid.backend.service

import com.vivid.backend.api.web.dto.*
import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.Feature
import com.vivid.backend.domain.entity.FeatureEnvironment
import com.vivid.backend.domain.entity.FeatureLink
import com.vivid.backend.domain.repository.FeatureEnvironmentRepository
import com.vivid.backend.domain.repository.FeatureRepository
import com.vivid.backend.domain.repository.TeamRepository
import com.vivid.backend.service.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class FeatureService(
    private val featureRepository: FeatureRepository,
    private val featureEnvironmentRepository: FeatureEnvironmentRepository,
    private val environmentService: EnvironmentService,
    private val teamRepository: TeamRepository,
    private val departmentService: DepartmentService
) {

    @Transactional(readOnly = true)
    fun getAllFeatures(
        departmentId: UUID,
        q: String?,
        pageable: Pageable,
    ): Page<Feature> {
        if (q == null) {
            return featureRepository.findAllByDepartmentId(departmentId, pageable)
        }
        return featureRepository.search(q, departmentId, pageable)
    }

    @Transactional(readOnly = true)
    fun getEnabledFeaturesForClient(environmentId: String, departmentId: UUID? = null): List<FeatureEnvironment> {
        val environment = if (departmentId != null) {
            environmentService.findEnvironment(environmentId, departmentId)
        } else {
            environmentService.findEnvironment(environmentId)
        } ?: throw ResourceNotFoundException("Environment with id $environmentId not found")
        return featureEnvironmentRepository.findAllByEnvironmentAndEnabledTrue(environment)
    }

    @Transactional(readOnly = true)
    fun getFeatureById(id: UUID, departmentId: UUID, environmentId: String?): Pair<Feature, FeatureEnvironment?> {
        val feature = findFeatureById(id, departmentId)
        val fe = environmentId?.let {
            val environment = environmentService.findEnvironment(it, departmentId)
                ?: throw ResourceNotFoundException("Environment with id $it not found")
            featureEnvironmentRepository.findByFeatureIdAndEnvironment(id, environment)
        }
        return feature to fe
    }

    @Transactional(readOnly = true)
    fun getFeatureByRunningNumber(runningNumber: Long, departmentId: UUID, environmentId: String?): Pair<Feature, FeatureEnvironment?> {
        val feature = featureRepository.findByRunningNumberAndDepartmentId(runningNumber, departmentId)
            .orElseThrow { ResourceNotFoundException("Feature with number $runningNumber not found in department $departmentId") }
        val fe = environmentId?.let {
            val environment = environmentService.findEnvironment(it, departmentId)
                ?: throw ResourceNotFoundException("Environment with id $it not found")
            featureEnvironmentRepository.findByFeatureIdAndEnvironment(feature.id, environment)
        }
        return feature to fe
    }

    @Transactional
    fun createFeature(departmentId: UUID, request: FeatureCreateRequest): Feature {
        val department = departmentService.findById(departmentId)
        val feature = request.toEntity(department)
        feature.runningNumber = featureRepository.getNextRunningNumber()
        return featureRepository.save(feature)
    }

    @Transactional
    fun updateFeature(id: UUID, departmentId: UUID, request: FeatureUpdateRequest): Pair<Feature, FeatureEnvironment?> {
        val feature = findFeatureById(id, departmentId)
        request.name?.let { feature.name = it }
        request.description?.let { feature.description = it }
        request.tags?.let { feature.tags = it.toMutableSet() }
        request.assignedTeamIds?.let { teamIds ->
            val teams = teamRepository.findAllById(teamIds)
            feature.assignedTeams = teams.toMutableSet()
        }
        val saved = featureRepository.save(feature)

        var fe: FeatureEnvironment? = null
        if (request.environmentId != null) {
            val environment = environmentService.findEnvironment(request.environmentId, departmentId)
                ?: throw ResourceNotFoundException("Environment with id ${request.environmentId} not found")
            fe = featureEnvironmentRepository.findByFeatureIdAndEnvironment(id, environment)
            if (fe == null) {
                fe = FeatureEnvironment(
                    feature = feature,
                    environment = environment,
                    enabled = request.enabled ?: false
                )
            }
            request.enabled?.let { fe.enabled = it }
            request.flags?.let { fe.flags = it.toMutableMap() }
            request.metadata?.let { fe.metadata = it }
            fe = featureEnvironmentRepository.save(fe)
        }

        return saved to fe
    }

    @Transactional
    fun upsertFeatureEnvironment(
        featureId: UUID,
        departmentId: UUID,
        environmentId: String,
        request: FeatureEnvironmentUpdateRequest,
    ): Pair<Feature, FeatureEnvironment> {
        val feature = findFeatureById(featureId, departmentId)
        val environment = environmentService.findEnvironment(environmentId, departmentId)
            ?: throw ResourceNotFoundException("Environment with id $environmentId not found")
        val fe = featureEnvironmentRepository.findByFeatureIdAndEnvironment(featureId, environment) ?: FeatureEnvironment(
                feature = feature,
                environment = environment,
                enabled = false
            )
        fe.enabled = request.enabled
        fe.flags = request.flags.toMutableMap()
        fe.metadata = request.metadata
        val savedFe = featureEnvironmentRepository.save(fe)
        return feature to savedFe
    }

    @Transactional
    fun deleteFeature(id: UUID, departmentId: UUID) {
        val feature = findFeatureById(id, departmentId)
        featureRepository.delete(feature)
    }

    @Transactional
    fun addFeatureLink(sourceId: UUID, departmentId: UUID, request: FeatureLinkCreateRequest): Feature {
        val sourceFeature = findFeatureById(sourceId, departmentId)
        val targetFeature = findFeatureById(request.targetFeatureId, departmentId)
        val link = FeatureLink(
            sourceFeature = sourceFeature,
            targetFeature = targetFeature,
            type = request.type
        )
        sourceFeature.outgoingLinks.add(link)
        return featureRepository.save(sourceFeature)
    }

    @Transactional
    fun removeFeatureLink(sourceId: UUID, departmentId: UUID, linkId: UUID): Feature {
        val sourceFeature = findFeatureById(sourceId, departmentId)
        sourceFeature.outgoingLinks.removeIf { it.id == linkId }
        return featureRepository.save(sourceFeature)
    }

    @Transactional(readOnly = true)
    fun getAllTags(departmentId: UUID): Set<String> = featureRepository.findAllDistinctTags(departmentId).toSet()


    private fun findFeatureById(id: UUID, departmentId: UUID): Feature {
        val feature = featureRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Feature with id $id not found") }
        if (feature.department.id != departmentId) {
            throw ResourceNotFoundException("Feature with id $id not found in this department")
        }
        return feature
    }

    @Transactional(readOnly = true)
    fun getFeatureByName(name: String, departmentId: UUID? = null, environment: String): FeatureEnvironment? {
        val feature = featureRepository.findByName(name) ?: return null
        if (departmentId != null && feature.department.id != departmentId) return null
        val environmentValue = feature.environments.find { it.environment.name == environment } ?: return null

        return environmentValue
    }
}
