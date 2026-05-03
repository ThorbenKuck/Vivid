package com.vivid.backend.service

import com.vivid.backend.api.web.dto.*
import com.vivid.backend.asKey
import com.vivid.backend.domain.entity.Feature
import com.vivid.backend.domain.entity.FeatureEnvironment
import com.vivid.backend.domain.entity.FeatureLink
import com.vivid.backend.domain.repository.FeatureEnvironmentRepository
import com.vivid.backend.domain.repository.FeatureRepository
import com.vivid.backend.service.exception.ResourceNotFoundException
import com.vivid.backend.toUuidOrNull
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class FeatureService(
    private val featureRepository: FeatureRepository,
    private val featureEnvironmentRepository: FeatureEnvironmentRepository,
    private val environmentService: EnvironmentService,
    private val userService: UserService,
    private val environmentStream: EnvironmentStream,
) {

    @Transactional(readOnly = true)
    fun getAllFeatures(
        q: String?,
        pageable: Pageable,
    ): Page<Feature> {
        if (q == null) {
            return featureRepository.findAll(pageable)
        }
        return featureRepository.search(q, pageable)
    }

    @Transactional(readOnly = true)
    fun getEnabledFeaturesForClient(environmentId: String): List<FeatureEnvironment> {
        val environment = environmentService.findEnvironment(environmentId)
            ?: throw ResourceNotFoundException("Environment with id $environmentId not found")
        return featureEnvironmentRepository.findAllByEnvironmentAndEnabledTrue(environment)
    }

    @Transactional(readOnly = true)
    fun getFeatureById(id: UUID): Feature {
        return findFeatureById(id)
    }

    @Transactional(readOnly = true)
    fun getFeatureByRunningNumber(runningNumber: Long): Feature {
        return featureRepository.findByRunningNumber(runningNumber)
            ?: throw ResourceNotFoundException("Feature with number $runningNumber not found")
    }

    private fun String.determineKey(): String {
        val key = asKey()
        featureRepository.findByKey(key) ?: return key

        var suffix = 0
        var keyWithSuffix = key + suffix
        while (featureRepository.findByKey(keyWithSuffix) != null) {
            ++suffix
            keyWithSuffix = key + suffix
        }

        return keyWithSuffix
    }

    @Transactional
    fun createFeature(
        name: String,
        description: String?,
        tags: List<String> = emptyList()
    ): Feature {
        val runningNumber = featureRepository.getNextRunningNumber()
        val key = name.determineKey()
        val feature = Feature(
            name = name,
            key = key,
            runningNumber = runningNumber,
            description = description,
            tags = tags.toMutableSet(),
        )
        environmentStream.pushFeature(feature)
        return featureRepository.save(feature)
    }

    @Transactional
    fun updateFeature(id: UUID, request: FeatureUpdateRequest): Feature {
        val feature = findFeatureById(id)
        request.name?.let { feature.name = it }
        request.description?.let { feature.description = it }
        request.tags?.let { feature.tags = it.toMutableSet() }

        request.environments?.forEach { envUpdate ->
            val environment = environmentService.findEnvironment(envUpdate.environmentId.toString())
                ?: throw ResourceNotFoundException("Environment with id ${envUpdate.environmentId} not found")

            val existing = feature.environments.find { it.environment.id == environment.id }
            val fe = existing ?: FeatureEnvironment(
                feature = feature,
                environment = environment,
                enabled = envUpdate.enabled
            ).also { feature.environments.add(it) }

            fe.enabled = envUpdate.enabled
            fe.flags = envUpdate.flags.toMutableMap()
            fe.metadata = envUpdate.metadata
        }

        environmentStream.pushFeature(feature)
        return featureRepository.save(feature)
    }

    @Transactional
    fun upsertFeatureEnvironment(
        featureId: UUID,
        environmentId: String,
        request: FeatureEnvironmentUpdateRequest,
    ): Feature {
        val feature = findFeatureById(featureId)
        val environment = environmentService.findEnvironment(environmentId)
            ?: throw ResourceNotFoundException("Environment with id $environmentId not found")

        val fe = feature.environments.find { it.environment.id == environment.id }
            ?: FeatureEnvironment(
                feature = feature,
                environment = environment,
                enabled = false
            ).also { feature.environments.add(it) }

        fe.enabled = request.enabled
        fe.flags = request.flags.toMutableMap()
        fe.metadata = request.metadata
        environmentStream.pushFeature(fe)

        return featureRepository.save(feature)
    }

    @Transactional
    fun deleteFeature(id: UUID) {
        val feature = findFeatureById(id)
        featureRepository.delete(feature)
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

    @Transactional
    fun addNote(featureId: UUID, authorId: UUID, request: NoteCreateRequest): Feature {
        val feature = findFeatureById(featureId)
        val author = userService.findById(authorId)
        val note = com.vivid.backend.domain.entity.Note(
            content = request.content,
            author = author,
            feature = feature
        )
        feature.notes.add(note)
        return featureRepository.save(feature)
    }

    private fun findFeatureById(id: UUID): Feature {
        return featureRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Feature with id $id not found") }
    }

    @Transactional(readOnly = true)
    fun findFeature(name: String, environmentIdentifier: String): FeatureEnvironment? {
        val environment = environmentService.findEnvironment(environmentIdentifier)
        val feature = name.toUuidOrNull()?.let { featureRepository.findByIdOrNull(it) }
            ?: featureRepository.findByKey(name)
            ?: name.toLongOrNull()?.let { featureRepository.findByRunningNumber(it) }
            ?: featureRepository.findByName(name)
            ?: return null

        val environmentValue = feature.environments.find { it.environment == environment } ?: return null
        return environmentValue
    }
}
