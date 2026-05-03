package com.vivid.backend.service

import com.vivid.backend.api.client.dto.toClientDto
import com.vivid.backend.domain.entity.EnvironmentEntity
import com.vivid.backend.domain.entity.FeatureEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event
import tools.jackson.databind.ObjectMapper
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = LoggerFactory.getLogger(EnvironmentStream::class.java)

@Component
class EnvironmentStream(
    private val objectMapper: ObjectMapper,
): DisposableBean {

    private val registration = ConcurrentHashMap<UUID, MutableList<EnvironmentSubscription>>()

    fun register(environment: EnvironmentEntity, applicationId: String?): SseEmitter {
        val registrations = registration.computeIfAbsent(environment.id) { mutableListOf() }
        val subscription = EnvironmentSubscription(applicationId)
        val emitter = subscription.sseEmitter
        registrations.add(subscription)
        emitter.onCompletion {
            logger.info("Removed SSE subscription for application \"{}\" at environment \"{}\"", subscription.applicationId, environment.id)
            registrations.remove(subscription)
        }
        emitter.onError {
            logger.error("Error in SSE subscription for application \"{}\" at environment \"{}\": {}", subscription.applicationId, environment.id, it.message)
            registrations.remove(subscription)
            emitter.complete()
        }
        emitter.onTimeout {
            logger.info("Timeout for SSE subscription for application \"{}\" at environment \"{}\"", subscription.applicationId, environment.id)
            registrations.remove(subscription)
            emitter.complete()
        }

        logger.info("Registered SSE subscription for application \"{}\" at environment \"{}\"({})", applicationId, environment.id, registrations.size)
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
            return
        }

        logger.debug("Pushing feature update to subscribers for environment \"{}\"", environmentId)
        val dto = feature.toClientDto(environmentId)
        val event = event()
            .data(objectMapper.writeValueAsString(dto), MediaType.APPLICATION_JSON)
            .id(UUID.randomUUID().toString())
            .name("feature-update")
        emitters.forEach {
            try {
                it.sseEmitter.send(event)
            } catch (e: Exception) {
                it.sseEmitter.completeWithError(e)
            }
        }
    }

    override fun destroy() {
        registration.values.forEach { it.forEach { it.sseEmitter.complete() } }
    }

    data class EnvironmentSubscription(
        val applicationId: String?,
        val sseEmitter: SseEmitter = SseEmitter(-1),
    )
}
