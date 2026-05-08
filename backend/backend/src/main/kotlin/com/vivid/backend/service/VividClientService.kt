package com.vivid.backend.service

import com.vivid.backend.api.MissingClientRegistrationException
import com.vivid.backend.api.MissingClientTokenException
import com.vivid.backend.domain.entity.VividClientEntity
import com.vivid.backend.domain.entity.VividClientPresenceEntity
import com.vivid.backend.domain.entity.infrastructure.FeatureEntity
import com.vivid.backend.domain.repository.VividClientPresenceRepository
import com.vivid.backend.domain.repository.VividClientRepository
import com.vivid.backend.domain.support.ApplicationIdentifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class VividClientService(
    private val repository: VividClientRepository,
    private val presenceRepository: VividClientPresenceRepository,
    private val settingsService: SettingsService,
    private val featureUsageService: FeatureUsageService,
) {

    @Transactional
    fun registerHeartbeat(
        clientName: String,
        environment: com.vivid.backend.domain.entity.EnvironmentEntity,
        clientToken: String?,
        technologies: Set<String>,
        clientVersion: String?
    ): VividClientEntity {
        val client = resolveClient(clientName, clientToken)

        val presence = presenceRepository.findByClientAndEnvironment(client, environment)
            ?: VividClientPresenceEntity(
                client = client,
                environment = environment
            )

        presence.lastSeen = Instant.now()
        presence.technologies = technologies.toMutableSet()
        presence.clientVersion = clientVersion ?: presence.clientVersion

        presenceRepository.save(presence)
        return client
    }

    @Transactional
    fun registerTechnologie(applicationIdentifier: ApplicationIdentifier, technologie: String) {
        val client = resolveClient(applicationIdentifier.applicationId, applicationIdentifier.token)
        val presence = presenceRepository.findByClientAndEnvironment(client, applicationIdentifier.environment)
            ?: VividClientPresenceEntity(
                client = client,
                environment = applicationIdentifier.environment
            )

        presence.technologies.add(technologie)
        presence.lastSeen = Instant.now()
        presenceRepository.save(presence)
    }

    @Transactional
    fun seen(
        applicationId: ApplicationIdentifier,
    ): VividClientEntity {
        val client = resolveClient(applicationId.applicationId, applicationId.token)
        val presence = presenceRepository.findByClientAndEnvironment(client, applicationId.environment)
            ?: VividClientPresenceEntity(
                client = client,
                environment = applicationId.environment
            )

        presence.lastSeen = Instant.now()
        presenceRepository.save(presence)
        return client
    }

    private fun resolveClient(
        clientName: String,
        clientToken: String?
    ): VividClientEntity {
        val settings = settingsService.getSettings()
        val client = if (settings.requireClientTokens) {
            val token = clientToken ?: throw MissingClientTokenException()
            repository.findByClientToken(token)
        } else {
            repository.findByClientName(clientName)
        }

        return client
            ?: if (settings.allowDynamicClientRegistration) {
                repository.save(
                    VividClientEntity(
                        clientName = clientName,
                        clientToken = clientToken ?: UUID.randomUUID().toString().replace("-", "")
                    )
                )
            } else {
                throw MissingClientRegistrationException()
            }
    }

    @Transactional(readOnly = true)
    fun getAllClients(): List<VividClientEntity> = repository.findAllWithPresences()

    @Transactional(readOnly = true)
    fun getClientsByEnvironment(environmentId: UUID): List<VividClientEntity> {
        return presenceRepository.findAllByEnvironmentId(environmentId).map { it.client }
    }

    @Transactional
    fun recordUsage(feature: FeatureEntity, applicationId: ApplicationIdentifier) {
        val client = resolveClient(applicationId.applicationId, applicationId.token)
        featureUsageService.recordUsage(feature, client, applicationId.environment)
    }

    @Transactional
    fun deleteClient(id: UUID) {
        repository.deleteById(id)
    }

    @Transactional
    fun updateClient(id: UUID, name: String, token: String?): VividClientEntity {
        val client = repository.findById(id).orElseThrow()
        client.clientName = name
        client.clientToken = token
        return repository.save(client)
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): VividClientEntity? {
        return repository.findById(id).orElse(null)
    }

    @Transactional
    fun createClient(name: String, token: String?): VividClientEntity {
        return repository.save(
            VividClientEntity(
                clientName = name,
                clientToken = token ?: UUID.randomUUID().toString().replace("-", "")
            )
        )
    }
}
