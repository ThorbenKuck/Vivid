package com.vivid.backend.service

import com.vivid.backend.api.web.dto.*
import com.vivid.backend.asKey
import com.vivid.backend.domain.entity.*
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
    private val environmentService: EnvironmentService,
    private val userService: UserService,
    private val environmentStream: EnvironmentStream,
) {

    @Transactional(readOnly = true)
    fun getAllFeatures(
        q: String?,
        pageable: Pageable,
    ): Page<FeatureEntity> {
        if (q == null) {
            return featureRepository.findAll(pageable)
        }
        return featureRepository.search(q, pageable)
    }

    @Transactional(readOnly = true)
    fun getEnabledFeaturesForClient(environmentId: String): List<FeatureEntity> {
        val environment = environmentService.findEnvironment(environmentId)
            ?: throw ResourceNotFoundException("Environment with id $environmentId not found")
        
        // Fetch all and filter in memory as instructed (access via feature entity)
        // Optimization: In a real scenario, we might want to use a custom query.
        return featureRepository.findAll().filter { it.resolve(environment.id).enabled }
    }

    @Transactional(readOnly = true)
    fun getFeatureById(id: UUID): FeatureEntity {
        return findFeatureById(id)
    }

    @Transactional(readOnly = true)
    fun getFeatureByRunningNumber(runningNumber: Long): FeatureEntity {
        return featureRepository.findByRunningNumber(runningNumber)
            ?: throw ResourceNotFoundException("FeatureEntity with number $runningNumber not found")
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
        request: FeatureCreateRequest
    ): FeatureEntity {
        val runningNumber = featureRepository.getNextRunningNumber()
        val key = request.name.determineKey()
        val feature = FeatureEntity(
            name = request.name,
            key = key,
            runningNumber = runningNumber,
            description = request.description,
            tags = request.tags.toMutableSet(),
            enabled = request.enabled,
            flags = request.flags.toMutableMap(),
            metadata = request.metadata
        )
        environmentStream.pushFeature(feature)
        return featureRepository.save(feature)
    }

    @Transactional
    fun updateFeature(id: UUID, request: FeatureUpdateRequest): FeatureEntity {
        val feature = findFeatureById(id)
        request.name?.let { feature.name = it }
        request.description?.let { feature.description = it }
        request.tags?.let { feature.tags = it.toMutableSet() }
        request.enabled?.let { feature.enabled = it }
        request.flags?.let { feature.flags = it.toMutableMap() }
        request.metadata?.let { feature.metadata = it }

        request.overrides?.forEach { overrideUpdate ->
            val environment = environmentService.findEnvironment(overrideUpdate.environmentId.toString())
                ?: throw ResourceNotFoundException("Environment with id ${overrideUpdate.environmentId} not found")

            val existing = feature.environmentOverrides.find { it.environment.id == environment.id }
            val override = existing ?: EnvironmentOverrideEntity(
                feature = feature,
                environment = environment
            ).also { feature.environmentOverrides.add(it) }

            overrideUpdate.enabled?.let { override.enabled = it }
            overrideUpdate.flags?.let { override.flags = it.toMutableMap() }
            overrideUpdate.metadata?.let { override.metadata = it }
            overrideUpdate.strategy?.let { override.strategy = it }
        }

        environmentStream.pushFeature(feature)
        return featureRepository.save(feature)
    }

    @Transactional
    fun upsertEnvironmentOverride(
        featureId: UUID,
        environmentId: String,
        request: EnvironmentOverrideUpdateRequest,
    ): FeatureEntity {
        val feature = findFeatureById(featureId)
        val environment = environmentService.findEnvironment(environmentId)
            ?: throw ResourceNotFoundException("Environment with id $environmentId not found")

        val override = feature.environmentOverrides.find { it.environment.id == environment.id }
            ?: EnvironmentOverrideEntity(
                feature = feature,
                environment = environment
            ).also { feature.environmentOverrides.add(it) }

        request.enabled?.let { override.enabled = it }
        request.flags?.let { override.flags = it.toMutableMap() }
        request.metadata?.let { override.metadata = it }
        request.strategy?.let { override.strategy = it }
        
        environmentStream.pushFeature(feature, environment.id)

        return featureRepository.save(feature)
    }

    @Transactional
    fun deleteFeature(id: UUID) {
        val feature = findFeatureById(id)
        featureRepository.delete(feature)
    }

    @Transactional
    fun addFeatureLink(sourceId: UUID, request: FeatureLinkCreateRequest): FeatureEntity {
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
    fun removeFeatureLink(sourceId: UUID, linkId: UUID): FeatureEntity {
        val sourceFeature = findFeatureById(sourceId)
        sourceFeature.outgoingLinks.removeIf { it.id == linkId }
        return featureRepository.save(sourceFeature)
    }

    @Transactional(readOnly = true)
    fun getAllTags(): Set<String> = featureRepository.findAllDistinctTags().toSet()

    @Transactional
    fun addNote(featureId: UUID, authorId: UUID, request: NoteCreateRequest): FeatureEntity {
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

    private fun findFeatureById(id: UUID): FeatureEntity {
        return featureRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("FeatureEntity with id $id not found") }
    }

    @Transactional(readOnly = true)
    fun findFeature(name: String, environmentIdentifier: String): FeatureEntity? {
        val environment = environmentService.findEnvironment(environmentIdentifier)
            ?: throw ResourceNotFoundException("Environment with id $environmentIdentifier not found")
            
        return name.toUuidOrNull()?.let { featureRepository.findByIdOrNull(it) }
            ?: featureRepository.findByKey(name)
            ?: name.toLongOrNull()?.let { featureRepository.findByRunningNumber(it) }
            ?: featureRepository.findByName(name)
    }
}
