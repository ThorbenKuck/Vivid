package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.*
import com.vivid.backend.service.EnvironmentService
import com.vivid.backend.service.FeatureService
import com.vivid.backend.service.exception.ResourceNotFoundException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/web/features")
@Tag(name = "Web Feature Management", description = "CRUD operations for features")
class WebFeatureController(
    private val featureService: FeatureService,
    private val environmentService: EnvironmentService,
) {

    @GetMapping
    @Operation(summary = "Get all features with optional search and environment context")
    fun getAllFeatures(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) environmentId: String?,
        pageable: Pageable
    ): Page<FeatureDto> {
        val environment = environmentId?.let { environmentService.requireEnvironment(it) }
        return featureService.getAllFeatures(q, null, pageable).map { it.toDto(environment) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feature by ID with optional environment context")
    fun getFeatureById(
        @PathVariable id: UUID,
        @RequestParam(required = false) environmentId: String?
    ): FeatureDto {
        try {
            return featureService.getFeatureById(id, environmentId).toDto()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new feature")
    fun createFeature(@RequestBody request: FeatureCreateRequest): FeatureDto {
        return featureService.createFeature(request).toDto()
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing feature (name, description, tags)")
    fun updateFeature(@PathVariable id: UUID, @RequestBody request: FeatureUpdateRequest): FeatureDto {
        return featureService.updateFeature(id, request).toDto()
    }

    @PutMapping("/{id}/environments/{environment}")
    @Operation(summary = "Upsert environment-specific state for a feature")
    fun upsertFeatureEnvironment(
        @PathVariable id: UUID,
        @PathVariable environment: String,
        @RequestBody request: FeatureEnvironmentUpdateRequest
    ): FeatureDto {
        return featureService.upsertFeatureEnvironment(id, environment, request).toDto()
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all distinct tags for autocomplete")
    fun getAllTags(): Set<String> = featureService.getAllTags()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a feature")
    fun deleteFeature(@PathVariable id: UUID) {
        featureService.deleteFeature(id)
    }

    @PostMapping("/{id}/links")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a link to another feature")
    fun addFeatureLink(@PathVariable id: UUID, @RequestBody request: FeatureLinkCreateRequest): FeatureDto {
        return featureService.addFeatureLink(id, request).toDto()
    }

    @DeleteMapping("/{id}/links/{linkId}")
    @Operation(summary = "Remove a link from a feature")
    fun removeFeatureLink(@PathVariable id: UUID, @PathVariable linkId: UUID): FeatureDto {
        return featureService.removeFeatureLink(id, linkId).toDto()
    }
}
