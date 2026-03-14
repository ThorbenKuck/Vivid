package com.vivid.backend.service

import com.vivid.backend.api.web.dto.*
import com.vivid.backend.domain.entity.Environment
import com.vivid.backend.domain.entity.Feature
import com.vivid.backend.domain.entity.FeatureEnvironment
import com.vivid.backend.domain.entity.FeatureLink
import com.vivid.backend.domain.repository.FeatureEnvironmentRepository
import com.vivid.backend.domain.repository.FeatureRepository
import com.vivid.backend.domain.repository.TeamRepository
import com.vivid.backend.service.exception.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class FeatureService(
    private val featureRepository: FeatureRepository,
    private val featureEnvironmentRepository: FeatureEnvironmentRepository,
    private val environmentService: EnvironmentService,
    private val teamRepository: TeamRepository
) {

    @Transactional(readOnly = true)
    fun getAllFeatures(
        q: String?,
        environment: Environment?,
        pageable: Pageable,
    ): Page<Feature> {
        if (q == null && environment == null) {
            return featureRepository.findAll(pageable)
        }
        return featureRepository.search(q, environment, pageable)
    }

    @Transactional(readOnly = true)
    fun getEnabledFeaturesForClient(environmentId: String): List<FeatureEnvironment> {
        val environment = environmentService.findEnvironment(environmentId)
            ?: throw ResourceNotFoundException("Environment with id $environmentId not found")
        return featureEnvironmentRepository.findAllByEnvironmentAndEnabledTrue(environment)
    }

    @Transactional(readOnly = true)
    fun getFeatureById(id: UUID, environmentId: String?): Pair<Feature, FeatureEnvironment?> {
        val feature = findFeatureById(id)
        val fe = environmentId?.let {
            val environment = environmentService.findEnvironment(it)
                ?: throw ResourceNotFoundException("Environment with id $it not found")
            featureEnvironmentRepository.findByFeatureIdAndEnvironment(id, environment)
        }
        return feature to fe
    }

    @Transactional
    fun createFeature(request: FeatureCreateRequest): Feature {
        val feature = request.toEntity()
        return featureRepository.save(feature)
    }

    @Transactional
    fun updateFeature(id: UUID, request: FeatureUpdateRequest): Pair<Feature, FeatureEnvironment?> {
        val feature = findFeatureById(id)
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
            val environment = environmentService.findEnvironment(request.environmentId)
                ?: throw ResourceNotFoundException("Environment with id ${request.environmentId} not found")
            fe = featureEnvironmentRepository.findByFeatureIdAndEnvironment(id, environment)
            if (fe == null) {
                fe = FeatureEnvironment(
                    feature = feature,
                    environment = Environment(id = request.environmentId, name = ""),
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
        environmentId: String,
        request: FeatureEnvironmentUpdateRequest,
    ): Pair<Feature, FeatureEnvironment> {
        val feature = findFeatureById(featureId)
        val environment = environmentService.findEnvironment(environmentId)
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
    fun deleteFeature(id: UUID) {
        if (!featureRepository.existsById(id)) {
            throw ResourceNotFoundException("Feature with id $id not found")
        }
        featureRepository.deleteById(id)
    }

    @Transactional
    fun addFeatureLink(sourceId: UUID, request: FeatureLinkCreateRequest): Feature {
        val sourceFeature = findFeatureById(sourceId)
        val targetFeature = findFeatureById(request.targetFeatureId)
        val link = FeatureLink(
            sourceFeature = sourceFeature,
            targetFeature = targetFeature,
            type = request.type
        )
        sourceFeature.outgoingLinks.add(link)
        return featureRepository.save(sourceFeature)
    }

    @Transactional
    fun removeFeatureLink(sourceId: UUID, linkId: UUID): Feature {
        val sourceFeature = findFeatureById(sourceId)
        sourceFeature.outgoingLinks.removeIf { it.id == linkId }
        return featureRepository.save(sourceFeature)
    }

    @Transactional(readOnly = true)
    fun getAllTags(): Set<String> = featureRepository.findAllDistinctTags().toSet()


    private fun findFeatureById(id: UUID): Feature {
        return featureRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Feature with id $id not found") }
    }

    @Transactional(readOnly = true)
    fun getFeatureByName(name: String, environment: String): FeatureEnvironment? {
        val feature = featureRepository.findByName(name) ?: return null
        val environmentValue = feature.environments.find { it.environment.name == environment } ?: return null

        return environmentValue
    }
}
