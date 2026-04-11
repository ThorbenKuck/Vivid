package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.*
import com.vivid.backend.service.EnvironmentService
import com.vivid.backend.service.FeatureService
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
    @Operation(summary = "Get all features with optional search")
    fun getAllFeatures(
        @RequestParam departmentId: UUID,
        @RequestParam(required = false) q: String?,
        pageable: Pageable
    ): Page<FeatureDto> {
        val allEnvironments = environmentService.getAll(departmentId)
        return featureService.getAllFeatures(departmentId, q, pageable).map { it.toDto(allEnvironments) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feature by ID")
    fun getFeatureById(
        @PathVariable id: UUID,
        @RequestParam departmentId: UUID
    ): FeatureDto {
        val allEnvironments = environmentService.getAll(departmentId)
        return featureService.getFeatureById(id, departmentId).toDto(allEnvironments)
    }

    @GetMapping("/number/{runningNumber}")
    @Operation(summary = "Get feature by running number")
    fun getFeatureByRunningNumber(
        @PathVariable runningNumber: Long,
        @RequestParam departmentId: UUID
    ): FeatureDto {
        val allEnvironments = environmentService.getAll(departmentId)
        return featureService.getFeatureByRunningNumber(runningNumber, departmentId).toDto(allEnvironments)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new feature")
    fun createFeature(
        @RequestParam departmentId: UUID,
        @RequestBody request: FeatureCreateRequest
    ): FeatureDto {
        val allEnvironments = environmentService.getAll(departmentId)
        return featureService.createFeature(departmentId, request).toDto(allEnvironments)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing feature (name, description, tags)")
    fun updateFeature(
        @PathVariable id: UUID,
        @RequestParam departmentId: UUID,
        @RequestBody request: FeatureUpdateRequest
    ): FeatureDto {
        val allEnvironments = environmentService.getAll(departmentId)
        return featureService.updateFeature(id, departmentId, request).toDto(allEnvironments)
    }

    @PutMapping("/{id}/environments/{environment}")
    @Operation(summary = "Upsert environment-specific state for a feature")
    fun upsertFeatureEnvironment(
        @PathVariable id: UUID,
        @PathVariable environment: String,
        @RequestParam departmentId: UUID,
        @RequestBody request: FeatureEnvironmentUpdateRequest
    ): FeatureDto {
        val allEnvironments = environmentService.getAll(departmentId)
        return featureService.upsertFeatureEnvironment(id, departmentId, environment, request).toDto(allEnvironments)
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all distinct tags for autocomplete")
    fun getAllTags(@RequestParam departmentId: UUID): Set<String> = featureService.getAllTags(departmentId)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a feature")
    fun deleteFeature(
        @PathVariable id: UUID,
        @RequestParam departmentId: UUID
    ) {
        featureService.deleteFeature(id, departmentId)
    }

    @PostMapping("/{id}/links")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a link to another feature")
    fun addFeatureLink(
        @PathVariable id: UUID,
        @RequestParam departmentId: UUID,
        @RequestBody request: FeatureLinkCreateRequest
    ): FeatureDto {
        val allEnvironments = environmentService.getAll(departmentId)
        return featureService.addFeatureLink(id, departmentId, request).toDto(allEnvironments)
    }

    @DeleteMapping("/{id}/links/{linkId}")
    @Operation(summary = "Remove a link from a feature")
    fun removeFeatureLink(
        @PathVariable id: UUID,
        @PathVariable linkId: UUID,
        @RequestParam departmentId: UUID
    ): FeatureDto {
        val allEnvironments = environmentService.getAll(departmentId)
        return featureService.removeFeatureLink(id, departmentId, linkId).toDto(allEnvironments)
    }
}
