package com.vivid.backend.api.client

import com.vivid.backend.api.client.dto.ClientFeatureDto
import com.vivid.backend.api.client.dto.toClientDto
import com.vivid.backend.service.EnvironmentService
import com.vivid.backend.service.EnvironmentStream
import com.vivid.backend.service.FeatureService
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
) {

    @GetMapping
    @Operation(summary = "Get all enabled features for runtime consumption in the given environment")
    fun getEnabledFeatures(
        @PathVariable environment: String,
        @RequestHeader(name = "X-Application-Id", required = true) appId: String,
    ): List<ClientFeatureDto> {
        val env = environmentService.findEnvironment(environment)
            ?: throw com.vivid.backend.service.exception.ResourceNotFoundException("Environment with id $environment not found")
        return featureService.getEnabledFeaturesForClient(environment).map { it.toClientDto(env.id) }
    }

    @GetMapping("/{key}")
    fun getFeature(
        @PathVariable environment: String,
        @PathVariable key: String,
        @RequestHeader(name = "X-Application-Id", required = true) appId: String,
    ): ResponseEntity<ClientFeatureDto> {
        val env = environmentService.findEnvironment(environment) ?: return ResponseEntity.notFound().build()
        return featureService.findFeature(name = key, environmentIdentifier = environment)
            ?.toClientDto(env.id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/stream")
    fun streamFeatures(
        @PathVariable environment: String,
        @RequestHeader(name = "X-Application-Id", required = true) appId: String,
    ): ResponseEntity<SseEmitter> {
        val environment = environmentService.findEnvironment(environment) ?: return ResponseEntity.notFound().build()
        val emitter = environmentStream.register(environment, appId)
        return ResponseEntity.ok(emitter)
    }
}
