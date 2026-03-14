package com.vivid.backend.api.client

import com.vivid.backend.api.client.dto.ClientFeatureDto
import com.vivid.backend.api.client.dto.toDto
import com.vivid.backend.service.EnvironmentService
import com.vivid.backend.service.FeatureService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/client/features/{environment}")
@Tag(name = "Client API", description = "Runtime API for SDKs")
class ClientFeatureController(
    private val featureService: FeatureService,
) {

    @GetMapping
    @Operation(summary = "Get all enabled features for runtime consumption in the given environment")
    fun getEnabledFeatures(
        @PathVariable environment: String,
    ): List<ClientFeatureDto> {
        return featureService.getEnabledFeaturesForClient(environment).map { it.toDto() }
    }

    @GetMapping("/{name}")
    fun getFeature(
        @PathVariable environment: String,
        @PathVariable name: String,
    ): ResponseEntity<ClientFeatureDto> {
        return featureService.getFeatureByName(name = name, environment = environment)
            ?.toDto()
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/stream")
    fun streamFeatures(
        @PathVariable environment: String,
    ) {

    }
}
