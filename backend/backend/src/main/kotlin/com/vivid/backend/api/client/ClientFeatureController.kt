package com.vivid.backend.api.client

import com.vivid.backend.api.client.dto.ClientFeatureDto
import com.vivid.backend.api.client.dto.toClientDto
import com.vivid.backend.domain.support.ApplicationIdentifier
import com.vivid.backend.service.*
import com.vivid.backend.service.exception.ResourceNotFoundException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/client/features/{environment}")
@Tag(name = "Client API", description = "Runtime API for SDKs")
class ClientFeatureController(
    private val featureService: FeatureService,
    private val environmentStream: EnvironmentStream,
    private val environmentService: EnvironmentService,
    private val clientService: VividClientService,
    private val featureUsageService: FeatureUsageService,
) {

    @GetMapping
    @Operation(summary = "Get all enabled features for runtime consumption in the given environment")
    fun getEnabledFeatures(
        @PathVariable environment: String,
        applicationIdentifier: ApplicationIdentifier?,
    ): List<ClientFeatureDto> {
        val client = applicationIdentifier?.let { clientService.seen(it) }
        val env = client?.environment
            ?: environmentService.findEnvironment(environment)
            ?: throw ResourceNotFoundException("Environment with id $environment not found")
        val features = featureService.getEnabledFeaturesForClient(environment)
        client?.let { c ->
            features.forEach { f -> featureUsageService.recordUsage(f, c) }
        }
        return features.map { it.toClientDto(env.id) }
    }

    @GetMapping("/{key}")
    fun getFeature(
        @PathVariable environment: String,
        @PathVariable key: String,
        applicationIdentifier: ApplicationIdentifier?,
    ): ResponseEntity<ClientFeatureDto> {
        val client = applicationIdentifier?.let { clientService.seen(it) }
        val env = environmentService.findEnvironment(environment) ?: return ResponseEntity.notFound().build()
        val feature = featureService.findFeature(name = key, environmentIdentifier = environment)
            ?: return ResponseEntity.notFound().build()

        client?.let { featureUsageService.recordUsage(feature, it) }

        return ResponseEntity.ok(feature.toClientDto(env.id))
    }

    @GetMapping("/stream")
    fun streamFeatures(
        @PathVariable environment: String,
        applicationIdentifier: ApplicationIdentifier?,
    ): ResponseEntity<SseEmitter> {
        if (applicationIdentifier == null) {
            return ResponseEntity.notFound().build()
        }
        clientService.seen(applicationIdentifier)
        val environment = environmentService.findEnvironment(environment) ?: return ResponseEntity.notFound().build()
        val emitter = environmentStream.register(environment, applicationIdentifier)
        return ResponseEntity.ok(emitter)
    }
}
