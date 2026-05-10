package com.vivid.backend.clients.streams

import com.vivid.backend.clients.streams.sse.SseClientStream
import com.vivid.backend.clients.streams.sse.SseClientStreamController
import com.vivid.backend.domain.repository.FeatureRepository
import com.vivid.backend.service.EnvironmentService
import com.vivid.backend.service.VividClientService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.Executors

private val logger = LoggerFactory.getLogger(ClientStreamConfiguration::class.java)

@Configuration
@ConditionalOnProperty(
    "application.clients.streams.enabled",
    havingValue = "true",
)
class ClientStreamConfiguration {

    init {
        logger.info("Client stream configuration enabled")
    }

    @Bean
    @ConditionalOnEnabledClientStream("sse")
    fun sseClientStream(
        objectMapper: ObjectMapper,
        featureRepository: FeatureRepository,
        clientService: VividClientService,
    ): SseClientStream {
        logger.info("SSE client stream is enabled")
        return SseClientStream(
            objectMapper,
            featureRepository,
            clientService,
        )
    }

    @Bean
    fun clientStreams(clientStreams: List<ClientStream>): ClientStreams? {
        if (clientStreams.isEmpty()) {
            logger.info("No Client Streams configured")
            return null
        }

        logger.info("Initializing ${clientStreams.size} Client Streams: $clientStreams")
        return ClientStreams(clientStreams, Executors.newFixedThreadPool(clientStreams.size))
    }
}
