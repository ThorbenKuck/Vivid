package com.vivid.backend.clients.streams.sse

import com.vivid.backend.clients.api.dto.toClientDto
import com.vivid.backend.clients.streams.ClientStream
import com.vivid.backend.clients.streams.dto.FeatureChangedEvent
import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.infrastructure.FeatureEntity
import com.vivid.backend.domain.repository.FeatureRepository
import com.vivid.backend.domain.support.ApplicationIdentifier
import com.vivid.backend.service.VividClientService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event
import tools.jackson.databind.ObjectMapper
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private val logger = LoggerFactory.getLogger(SseClientStream::class.java)

class SseClientStream(
    private val objectMapper: ObjectMapper,
    private val featureRepository: FeatureRepository,
    private val clientService: VividClientService,
) : ClientStream {

    private val registration = ConcurrentHashMap<UUID, CopyOnWriteArrayList<EnvironmentSubscription>>()

    override fun shutdown() {
        logger.info("[SseClientStream] Shutting down SSE client stream")
        registration.values.forEach {
            it.forEach {
                logger.debug("[SseClientStream] Completing SSE subscription for application \"{}\"", it.applicationId)
                try {
                    it.sseEmitter.complete()
                    logger.debug("[SseClientStream] [OK] SSE subscription for application \"{}\"", it.applicationId)
                } catch (e: Exception) {
                    logger.warn(
                        "[SseClientStream] Error completing SSE subscription for application \"{}\": {}",
                        it.applicationId,
                        e.message
                    )
                }
            }
        }
        registration.clear()
    }

    override fun push(event: FeatureChangedEvent) {
        val feature = featureRepository.findById(event.featureId).orElse(null)
        if (feature == null) {
            logger.warn("[SseClientStream] Received feature update for non-existing feature: {}", event.featureId)
            return
        }

        if (event.environmentIds == null) {
            logger.debug("[SseClientStream] Pushing feature update to all environments")
            pushFeature(feature)
        } else {
            logger.debug("[SseClientStream] Pushing feature update to environments: {}", event.environmentIds)
            event.environmentIds.forEach { envId ->
                pushFeature(feature, envId)
            }
        }
    }

    fun register(
        environment: EnvironmentEntity,
        applicationIdentifier: ApplicationIdentifier?,
    ): SseEmitter {
        applicationIdentifier?.let { clientService.registerTechnologie(it, "sse") }
        val registrations = registration.computeIfAbsent(environment.id) { CopyOnWriteArrayList() }
        val subscription = EnvironmentSubscription(applicationIdentifier)
        val emitter = subscription.sseEmitter
        registrations.add(subscription)
        emitter.onCompletion {
            logger.info("[SseClientStream] Removed SSE subscription for application \"{}\"", subscription.applicationId)
            registrations.remove(subscription)
        }
        emitter.onError {
            logger.error("[SseClientStream] Error in SSE subscription for application \"{}\": {}", subscription.applicationId, it.message)
            registrations.remove(subscription)
            emitter.complete()
        }
        emitter.onTimeout {
            logger.info("[SseClientStream] Timeout for SSE subscription for application \"{}\"", subscription.applicationId)
            registrations.remove(subscription)
            emitter.complete()
        }

        logger.info(
            "[SseClientStream] Registered SSE subscription for application \"{}\". Active registrations: {}",
            applicationIdentifier,
            registrations.size
        )
        return subscription.sseEmitter
    }

    fun pushFeature(feature: FeatureEntity) {
        registration.keys.forEach { envId ->
            pushFeature(feature, envId)
        }
    }

    fun pushFeature(feature: FeatureEntity, environmentId: UUID) {
        val emitters = registration[environmentId] ?: return

        if (emitters.isEmpty()) {
            logger.debug("[SseClientStream] No subscribers for environment \"{}\"", environmentId)
            return
        }

        logger.debug("[SseClientStream] Pushing feature update to {} subscribers for environment \"{}\"", emitters.size, environmentId)
        val dto = feature.toClientDto(environmentId)
        val event = event()
            .data(objectMapper.writeValueAsString(dto), MediaType.APPLICATION_JSON)
            .id(UUID.randomUUID().toString())
            .name("feature-update")
        emitters.forEach {
            try {
                it.sseEmitter.send(event)

                it.applicationId?.let { applicationId ->
                    try {
                        clientService.recordUsage(feature, applicationId)
                    } catch (_: Exception) {
                    }
                }
            } catch (e: Exception) {
                it.sseEmitter.completeWithError(e)
            }
        }
    }

    data class EnvironmentSubscription(
        val applicationId: ApplicationIdentifier?,
        val sseEmitter: SseEmitter = SseEmitter(-1),
    )

    override fun toString(): String {
        return "SseClientStream()"
    }
}
