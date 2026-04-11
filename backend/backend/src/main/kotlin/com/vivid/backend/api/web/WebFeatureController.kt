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
    @Operation(summary = "Get all features with optional search and environment context")
    fun getAllFeatures(
        @RequestParam departmentId: UUID,
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) environmentId: String?,
        pageable: Pageable
    ): Page<FeatureDto> {
        val environment by lazy { environmentId?.let { environmentService.findEnvironment(it, departmentId) } }
        try {
            return featureService.getAllFeatures(departmentId, q, pageable).map { it.toDto(environment) }
        } catch (t: Throwable) {
            t.printStackTrace()
            throw t
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feature by ID with optional environment context")
    fun getFeatureById(
        @PathVariable id: UUID,
        @RequestParam departmentId: UUID,
        @RequestParam(required = false) environmentId: String?
    ): FeatureDto {
        return featureService.getFeatureById(id, departmentId, environmentId).toDto()
    }

    @GetMapping("/number/{runningNumber}")
    @Operation(summary = "Get feature by running number with optional environment context")
    fun getFeatureByRunningNumber(
        @PathVariable runningNumber: Long,
        @RequestParam departmentId: UUID,
        @RequestParam(required = false) environmentId: String?
    ): FeatureDto {
        return featureService.getFeatureByRunningNumber(runningNumber, departmentId, environmentId).toDto()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new feature")
    fun createFeature(
        @RequestParam departmentId: UUID,
        @RequestBody request: FeatureCreateRequest
    ): FeatureDto {
        return featureService.createFeature(departmentId, request).toDto()
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing feature (name, description, tags)")
    fun updateFeature(
        @PathVariable id: UUID,
        @RequestParam departmentId: UUID,
        @RequestBody request: FeatureUpdateRequest
    ): FeatureDto {
        return featureService.updateFeature(id, departmentId, request).toDto()
    }

    @PutMapping("/{id}/environments/{environment}")
    @Operation(summary = "Upsert environment-specific state for a feature")
    fun upsertFeatureEnvironment(
        @PathVariable id: UUID,
        @PathVariable environment: String,
        @RequestParam departmentId: UUID,
        @RequestBody request: FeatureEnvironmentUpdateRequest
    ): FeatureDto {
        return featureService.upsertFeatureEnvironment(id, departmentId, environment, request).toDto()
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
        return featureService.addFeatureLink(id, departmentId, request).toDto()
    }

    @DeleteMapping("/{id}/links/{linkId}")
    @Operation(summary = "Remove a link from a feature")
    fun removeFeatureLink(
        @PathVariable id: UUID,
        @PathVariable linkId: UUID,
        @RequestParam departmentId: UUID
    ): FeatureDto {
        return featureService.removeFeatureLink(id, departmentId, linkId).toDto()
    }
}
