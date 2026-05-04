package com.vivid.backend.service

import com.vivid.backend.api.MissingClientRegistrationException
import com.vivid.backend.api.MissingClientTokenException
import com.vivid.backend.domain.entity.VividClientEntity
import com.vivid.backend.domain.entity.infrastructure.FeatureEntity
import com.vivid.backend.domain.repository.VividClientRepository
import com.vivid.backend.domain.support.ApplicationIdentifier
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class VividClientService(
    private val repository: VividClientRepository,
    private val settingsService: SettingsService,
    private val featureUsageService: FeatureUsageService,
) {

    @Transactional
    fun registerHeartbeat(
        newEntity: VividClientEntity,
    ): VividClientEntity? {
        return resolveEntity(newEntity).updateBy(newEntity)
    }

    @Transactional
    fun registerTechnologie(applicationIdentifier: ApplicationIdentifier, technologie: String) {
        val entity = resolveEntity(applicationIdentifier)
        entity.technologies.add(technologie)
        entity.lastSeen = Instant.now()
    }

    @Transactional
    fun seen(
        applicationId: ApplicationIdentifier,
    ): VividClientEntity {
        val entity = resolveEntity(applicationId)
        entity.lastSeen = Instant.now()
        return entity
    }

    private fun resolveEntity(
        newEntity: VividClientEntity
    ): VividClientEntity {
        val settings = settingsService.getSettings()
        val client = if (settings.requireClientTokens) {
            val clientToken = newEntity.clientToken
                ?: throw MissingClientTokenException()
            repository.findByClientTokenAndEnvironment(clientToken, newEntity.environment)
        } else {
            repository.findByClientNameAndEnvironment(newEntity.clientName, newEntity.environment)
        }

        return client
            ?: if (settings.allowDynamicClientRegistration) {
                repository.save(newEntity)
            } else {
                throw MissingClientRegistrationException()
            }
    }

    fun resolveEntity(applicationIdentifier: ApplicationIdentifier): VividClientEntity {
        return resolveEntity(
            VividClientEntity(
                clientName = applicationIdentifier.applicationId,
                environment = applicationIdentifier.environment,
                clientToken = applicationIdentifier.token,
                lastSeen = Instant.now(),
            )
        )
    }

    @Transactional(readOnly = true)
    fun getAllClients(): List<VividClientEntity> = repository.findAll()

    @Transactional(readOnly = true)
    fun getClientsByEnvironment(environmentId: UUID): List<VividClientEntity> =
        repository.findAllByEnvironmentId(environmentId)

    @Transactional
    fun recordUsage(feature: FeatureEntity, applicationId: ApplicationIdentifier) {
        val entity = resolveEntity(applicationId)
        featureUsageService.recordUsage(feature, entity)
    }
}
