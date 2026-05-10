package com.vivid.sdk.spring.rest

import com.vivid.sdk.HeartbeatApi
import com.vivid.clients.api.Heartbeat
import com.vivid.sdk.spring.VividProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.http.MediaType
import org.springframework.scheduling.TaskScheduler
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClient
import tools.jackson.databind.ObjectMapper
import java.time.Instant

private val logger = LoggerFactory.getLogger(VividHeartbeat::class.java)

class VividHeartbeat(
    private val vividProperties: VividProperties,
    private val restProperties: VividRestProperties,
    private val executor: TaskScheduler,
    private val restClient: RestClient,
    private val objectMapper: ObjectMapper,
) : InitializingBean, HeartbeatApi {
    override fun afterPropertiesSet() {
        logger.debug("Starting heartbeat with interval: {}", restProperties.heartbeat.interval)
        executor.scheduleAtFixedRate({
            sendHeartbeat()
        }, Instant.now().plus(restProperties.heartbeat.interval), restProperties.heartbeat.interval)
    }

    override fun sendHeartbeat() {
        sendHeartbeat(
            Heartbeat(
                vividProperties.applicationName,
                vividProperties.environment,
                vividProperties.clientToken,
                vividProperties.streams,
                null, // TODO: Fetch from API using Manifest entries (Implementation-Version)
            )
        )
    }

    override fun sendHeartbeat(heartbeat: Heartbeat) {
        logger.trace("Sending heartbeat")

        try {

            val entity = restClient.post()
                .uri {
                    it.path("/api/client/heartbeat").build()
                }
                .body {
                    it.write(objectMapper.writeValueAsBytes(heartbeat))
                }
                .headers {
                    it.accept = listOf(MediaType.APPLICATION_JSON)
                    it.contentType = MediaType.APPLICATION_JSON
                    restProperties.headerNames.applyTo(it, vividProperties)
                }
                .retrieve()
                .toBodilessEntity()

            if (entity.statusCode.isError) {
                logger.warn("Failed to send heartbeat: {}", entity.statusCode)
            } else {
                logger.trace("Send Heartbeat")
            }
        } catch (e: HttpStatusCodeException) {
            logger.warn("Failed to send heartbeat: {}", e.message)
        }
    }
}
