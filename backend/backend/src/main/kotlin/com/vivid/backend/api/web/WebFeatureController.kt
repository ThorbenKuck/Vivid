package com.vivid.backend.api.web

import com.vivid.backend.api.web.dto.*
import com.vivid.backend.service.EnvironmentService
import com.vivid.backend.service.FeatureService
import com.vivid.backend.service.PermissionService
import com.vivid.backend.service.UserService
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
    private val userService: UserService
) {

    @GetMapping
    @Operation(summary = "Get all features with optional search")
    fun getAllFeatures(
        @RequestParam(required = false) q: String?,
        pageable: Pageable
    ): PageDto<FeatureDto> {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        return featureService.getAllFeatures(q, pageable).toDto { it.toDto(allEnvironments) }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feature by ID")
    fun getFeatureById(
        @PathVariable id: UUID
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        return featureService.getFeatureById(id).toDto(allEnvironments)
    }

    @GetMapping("/number/{runningNumber}")
    @Operation(summary = "Get feature by running number")
    fun getFeatureByRunningNumber(
        @PathVariable runningNumber: Long
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        return featureService.getFeatureByRunningNumber(runningNumber).toDto(allEnvironments)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new feature")
    fun createFeature(
        @RequestBody request: FeatureCreateRequest
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        return featureService.createFeature(request).toDto(allEnvironments)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing feature (name, description, tags, global settings, overrides)")
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
        return featureService.updateFeature(id, request).toDto(allEnvironments)
    }

    @PutMapping("/{id}/environments/{environment}")
    @Operation(summary = "Upsert environment-specific override for a feature")
    @PreAuthorize("@permissionService.hasEnvPermission(#environment, 'write')")
    fun upsertEnvironmentOverride(
        @PathVariable id: UUID,
        @PathVariable environment: String,
        @RequestBody request: EnvironmentOverrideUpdateRequest
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        return featureService.upsertEnvironmentOverride(id, environment, request).toDto(allEnvironments)
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all distinct tags for autocomplete")
    fun getAllTags(): Set<String> = featureService.getAllTags()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a feature")
    fun deleteFeature(
        @PathVariable id: UUID
    ) {
        featureService.deleteFeature(id)
    }

    @PostMapping("/{id}/links")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a link to another feature")
    fun addFeatureLink(
        @PathVariable id: UUID,
        @RequestBody request: FeatureLinkCreateRequest
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        return featureService.addFeatureLink(id, request).toDto(allEnvironments)
    }

    @DeleteMapping("/{id}/links/{linkId}")
    @Operation(summary = "Remove a link from a feature")
    fun removeFeatureLink(
        @PathVariable id: UUID,
        @PathVariable linkId: UUID
    ): FeatureDto {
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        return featureService.removeFeatureLink(id, linkId).toDto(allEnvironments)
    }

    @PostMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a note to a feature")
    fun addNote(
        @PathVariable id: UUID,
        @RequestBody request: NoteCreateRequest,
        authentication: org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
    ): FeatureDto {
        val keycloakId = authentication.token.subject
        val user = userService.findByKeycloakId(keycloakId)
            ?: throw com.vivid.backend.service.exception.ResourceNotFoundException("User not found")
        val allEnvironments = permissionService.filterVisibleEnvironments(environmentService.getAll())
        return featureService.addNote(id, user.id!!, request).toDto(allEnvironments)
    }
}
