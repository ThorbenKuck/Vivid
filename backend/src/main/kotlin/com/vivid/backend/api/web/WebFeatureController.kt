package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.*
import com.vivid.backend.service.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/web/features")
@Tag(name = "Web FeatureEntity Management", description = "CRUD operations for features")
class WebFeatureController(
    private val featureService: FeatureService,
    private val environmentService: EnvironmentService,
    private val permissionService: PermissionService,
    private val userService: UserService,
    private val featureUsageService: FeatureUsageService
) {

    @GetMapping
    @Operation(summary = "Get all features with optional search")
    @PreAuthorize("@permissionService.hasPermission('features', 'read')")
    fun getAllFeatures(
        @RequestParam(required = false) q: String?,
        pageable: Pageable
    ): PageDto<FeatureDto> {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        return featureService.getAllFeatures(q, pageable).toDto { it.toDto(allEnvironments) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feature by ID")
    @PreAuthorize("@permissionService.hasPermission('features', 'read')")
    fun getFeatureById(
        @PathVariable id: UUID
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        val feature = featureService.getFeatureById(id)
        val usage = featureUsageService.getUsageForFeature(feature.id)
        return feature.toDto(allEnvironments, usage)
    }

    @GetMapping("/number/{runningNumber}")
    @Operation(summary = "Get feature by running number")
    @PreAuthorize("@permissionService.hasPermission('features', 'read')")
    fun getFeatureByRunningNumber(
        @PathVariable runningNumber: Long
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        val feature = featureService.getFeatureByRunningNumber(runningNumber)
        val usage = featureUsageService.getUsageForFeature(feature.id)
        return feature.toDto(allEnvironments, usage)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new feature")
    @PreAuthorize("@permissionService.hasPermission('features', 'write')")
    fun createFeature(
        @RequestBody request: FeatureCreateRequest
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        val feature = featureService.createFeature(request)
        return feature.toDto(allEnvironments)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing feature (name, description, tags, global settings, overrides)")
    @PreAuthorize("@permissionService.hasPermission('features', 'write')")
    fun updateFeature(
        @PathVariable id: UUID,
        @RequestBody request: FeatureUpdateRequest
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        // Validate write access for each environment in request
        request.overrides?.forEach { overrideUpdate ->
            val env = environmentService.findEnvironment(overrideUpdate.environmentId.toString())
                ?: throw com.vivid.backend.service.exception.ResourceNotFoundException("Environment not found: ${overrideUpdate.environmentId}")
            if (!permissionService.hasEnvPermission(env.name, "write")) {
                throw org.springframework.security.access.AccessDeniedException("No write access for environment: ${env.name}")
            }
        }
        val feature = featureService.updateFeature(id, request)
        val usage = featureUsageService.getUsageForFeature(feature.id)
        return feature.toDto(allEnvironments, usage)
    }

    @PutMapping("/{id}/environments/{envId}")
    @Operation(summary = "Upsert environment-specific override for a feature")
    @PreAuthorize("@permissionService.hasEnvPermission(#envId, 'write')")
    fun upsertEnvironmentOverride(
        @PathVariable id: UUID,
        @PathVariable envId: String,
        @RequestBody request: EnvironmentOverrideUpdateRequest
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        val feature = featureService.upsertEnvironmentOverride(id, envId, request)
        val usage = featureUsageService.getUsageForFeature(feature.id)
        return feature.toDto(allEnvironments, usage)
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all distinct tags for autocomplete")
    @PreAuthorize("@permissionService.hasPermission('features', 'read')")
    fun getAllTags(): Set<String> = featureService.getAllTags()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a feature")
    @PreAuthorize("@permissionService.hasPermission('features', 'write')")
    fun deleteFeature(
        @PathVariable id: UUID
    ) {
        featureService.deleteFeature(id)
    }

    @PostMapping("/{id}/links")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a link to another feature")
    @PreAuthorize("@permissionService.hasPermission('features', 'write')")
    fun addFeatureLink(
        @PathVariable id: UUID,
        @RequestBody request: FeatureLinkCreateRequest
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        val feature = featureService.addFeatureLink(id, request)
        val usage = featureUsageService.getUsageForFeature(feature.id)
        return feature.toDto(allEnvironments, usage)
    }

    @DeleteMapping("/{id}/links/{linkId}")
    @Operation(summary = "Remove a link from a feature")
    @PreAuthorize("@permissionService.hasPermission('features', 'write')")
    fun removeFeatureLink(
        @PathVariable id: UUID,
        @PathVariable linkId: UUID
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        val feature = featureService.removeFeatureLink(id, linkId)
        val usage = featureUsageService.getUsageForFeature(feature.id)
        return feature.toDto(allEnvironments, usage)
    }

    @PostMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a note to a feature")
    @PreAuthorize("@permissionService.hasPermission('features', 'write')")
    fun addNote(
        @PathVariable id: UUID,
        @RequestBody request: NoteCreateRequest,
        authentication: org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
    ): FeatureDto {
        val keycloakId = authentication.token.subject
        val user = userService.findByKeycloakId(keycloakId)
            ?: throw com.vivid.backend.service.exception.ResourceNotFoundException("User not found")
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        val feature = featureService.addNote(id, user.id!!, request)
        val usage = featureUsageService.getUsageForFeature(feature.id)
        return feature.toDto(allEnvironments, usage)
    }
}
